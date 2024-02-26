package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.demo.auth.provider.JwtTokenProvider;
import com.example.demo.domain.RefreshToken;
import com.example.demo.dto.Token;
import com.example.demo.enumData.TokenCode;
import com.example.demo.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor //해당 클래스의 final 필드나 @NonNull이 붙은 필드에 대한 생성자를 자동으로 생성해주는 역할을 합니다. 없으면 final 변수는 무조건 초기화 필요
public class JwtService {
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	
	@Transactional
	public void login(Token tokenDto) {
		RefreshToken refreshToken = RefreshToken.builder().loginId(tokenDto.getKey()).refreshToken(tokenDto.getRefreshToken()).accessToken(tokenDto.getAccessToken()).build();
		String loginId = refreshToken.getLoginId();
		if (refreshTokenRepository.existsByLoginId(loginId)) {
			refreshTokenRepository.deleteByLoginId(loginId);
		}
		refreshTokenRepository.save(refreshToken);
	}

	public Optional<RefreshToken> getRefreshToken(String refreshToken, ServletRequest request) {
		Optional<RefreshToken> dbRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken);

		if (dbRefreshToken == null) {
			request.setAttribute("exception", TokenCode.WRONG_TYPE_TOKEN.getCode());
		}
		return refreshTokenRepository.findByRefreshToken(refreshToken);
	}

	public Optional<RefreshToken> getAccessToken(String refreshToken) {
		return refreshTokenRepository.findByAccessToken(refreshToken);
	}

	@Transactional
	public Map<String, Object> validateRefreshToken(String refreshToken, ServletRequest request) {
		RefreshToken dbRefreshToken = null;
		String createdAccessToken = null;
		if (getRefreshToken(refreshToken, request).isEmpty()) {
			request.setAttribute("exception", TokenCode.UNKNOWN_ERROR.getCode());
		} else {
			dbRefreshToken = getRefreshToken(refreshToken, request).get();
			if (dbRefreshToken == null) {
				request.setAttribute("exception", TokenCode.UNKNOWN_ERROR.getCode());
			}
			createdAccessToken = jwtTokenProvider.validateRefreshAndCreateToken(dbRefreshToken, request);
			dbRefreshToken.setAccessToken(createdAccessToken);
		}
		return createRefreshJson(createdAccessToken, request);
	}

	public Map<String, Object> createRefreshJson(String createdAccessToken, ServletRequest request) {

		Map<String, Object> map = new HashMap<>();
		if (createdAccessToken == null) {
			map.put("code", TokenCode.UNKNOWN_ERROR.getCode());
			map.put("message", TokenCode.UNKNOWN_ERROR.getMessage());
			map.put("exception", "입력하신 Refresh Token을 확인해주세요.");
			return map;
		}
		// 기존 accessToken 새로운 Access Token으로 발급
		map.put("message", "Refresh 토큰을 통한 Access Token 생성이 완료되었습니다.");
		map.put("accessToken", createdAccessToken);
		return map;
	}
}
