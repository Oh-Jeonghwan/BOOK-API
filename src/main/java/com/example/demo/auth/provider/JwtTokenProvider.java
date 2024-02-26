package com.example.demo.auth.provider;

import java.security.Key;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.example.demo.domain.RefreshToken;
import com.example.demo.dto.Token;
import com.example.demo.enumData.TokenCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
	
	private String secretKey = "nmphrs";
	private String refreshSecretKey = "nmphrsrefresh";
	private Key accesssKey =  Keys.secretKeyFor(SignatureAlgorithm.HS512); 
	private Key refreshKey =  Keys.secretKeyFor(SignatureAlgorithm.HS512); 
	
	// 토큰 유효시간 30분
	private long tokenValidTime = 30 * 60 * 1000L;
	// refreshToken 14일
	private long refreshTokenValidTime = 14 * 1000 * 60 * 60 * 24;

	private final UserDetailsService userDetailsService;

	@PostConstruct
	protected void init() {
		secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
		refreshSecretKey = Base64.getEncoder().encodeToString(refreshSecretKey.getBytes());
	}

	public Token createToken(String loginId, Long accountId, Collection<? extends GrantedAuthority> role) {
		Claims claims = Jwts.claims().setSubject(loginId); // claims: JWT payload에 저장되는 정보단위
		
		claims.put("roles", role); // 정보는 key/value 쌍으로 저장된다.
		
		Date now = new Date();
		// Access Token
		String accessToken = Jwts.builder().setClaims(claims) // 정보 저장
				.setIssuedAt(now) // 토큰 발행 시간 정보
				.setExpiration(new Date(now.getTime() + tokenValidTime)) // set Expire Time
				.signWith(accesssKey) // 사용할 암호화 알고리즘과 signature에 들어갈 secret값 세팅
				.compact();

		// Refresh Token
		String refreshToken = Jwts.builder().setClaims(claims) // 정보 저장
				.setIssuedAt(now) // 토큰 발행 시간 정보
				.setExpiration(new Date(now.getTime() + refreshTokenValidTime)) // set Expire Time
				.signWith(refreshKey) // 사용할 암호화 알고리즘과
				// signature 에 들어갈 secret값 세팅
				.compact();

		return Token.builder().accessToken(accessToken).refreshToken(refreshToken).key(loginId).accountId(accountId).build();
	}

	// JWT 토큰에서 인증 정보 조회
	public UsernamePasswordAuthenticationToken getAuthentication(String token) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}

	// 토큰에서 회원 정보 추출
	public String getUserPk(String token) {
		return Jwts.parserBuilder().setSigningKey(accesssKey).build().parseClaimsJws(token).getBody().getSubject();
				//parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
	}

	public String resolveToken(HttpServletRequest request) {
		return request.getHeader("Authorization");
	}

	// 토큰의 유효성 +만료일자 확인
	public boolean validateToken(String jwtToken, ServletRequest request) {
		try {
			Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(accesssKey).build().parseClaimsJws(jwtToken);
			//setSigningKey 토큰 생성 시 사용했던 secretKey로 해야한다.
			return !claims.getBody().getExpiration().before(new Date());
		} catch (AccessDeniedException e) {
			// 권한 에러
			request.setAttribute("exception", TokenCode.ACCESS_DENIED.getCode());
		} catch (ExpiredJwtException e) {
			// 만료 에러
			request.setAttribute("exception", TokenCode.EXPIRED_TOKEN.getCode());
		} catch (MalformedJwtException e) {
			// 변조 에러
			request.setAttribute("exception", TokenCode.WRONG_TYPE_TOKEN.getCode());
		} catch (SignatureException e) {
			// 서명 에러
			request.setAttribute("exception", TokenCode.WRONG_TYPE_TOKEN.getCode());
		} catch (UnsupportedJwtException e) {
			// 형식 에러
			request.setAttribute("exception", TokenCode.UNSUPPORTED_TOKEN.getCode());
		} catch (IllegalArgumentException e) {
			// Jwt claims 에러
			request.setAttribute("exception", TokenCode.UNSUPPORTED_TOKEN.getCode());
		} catch (JwtException e) {
			request.setAttribute("exception", TokenCode.UNKNOWN_ERROR.getCode());
		}
		return false;
	}

	// refresh 토큰의 유효성 +만료일자 확인
	public String validateRefreshAndCreateToken(RefreshToken refreshTokenObj, ServletRequest request) {
		// refresh 객체에서 refreshToken 추출
		String refreshToken = refreshTokenObj.getRefreshToken();
		try {
			// 검증
			Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(refreshToken);

			// refresh 토큰의 만료시간이 지나지 않았을 경우, 새로운 access 토큰을 생성합니다.
			if (!claims.getBody().getExpiration().before(new Date())) {
				return recreationAccessToken(claims.getBody().get("sub").toString(), claims.getBody().get("roles"));
			}
		} catch (AccessDeniedException e) {
			// 권한 에러
			request.setAttribute("exception", TokenCode.ACCESS_DENIED.getCode());
		} catch (ExpiredJwtException e) {
			// 만료 에러
			request.setAttribute("exception", TokenCode.EXPIRED_TOKEN.getCode());
		} catch (MalformedJwtException e) {
			// 변조 에러
			request.setAttribute("exception", TokenCode.WRONG_TYPE_TOKEN.getCode());
		} catch (SignatureException e) {
			// 서명 에러
			request.setAttribute("exception", TokenCode.WRONG_TYPE_TOKEN.getCode());
		} catch (UnsupportedJwtException e) {
			// 형식 에러
			request.setAttribute("exception", TokenCode.UNSUPPORTED_TOKEN.getCode());
		} catch (IllegalArgumentException e) {
			// Jwt claims 에러
			request.setAttribute("exception", TokenCode.UNSUPPORTED_TOKEN.getCode());
		} catch (JwtException e) {
			request.setAttribute("exception", TokenCode.UNKNOWN_ERROR.getCode());
		}
		return null;
	}

	public String recreationAccessToken(String loginId, Object roles) {
		Claims claims = Jwts.claims().setSubject(loginId); // JWT payload 에 저장되는 정보단위
		claims.put("roles", roles); // 정보는 key / value 쌍으로 저장된다.
		Date now = new Date();

		// Access Token
		String accessToken = Jwts.builder().setClaims(claims) // 정보 저장
				.setIssuedAt(now) // 토큰 발행 시간 정보
				.setExpiration(new Date(now.getTime() + tokenValidTime)) // set Expire Time
				.signWith(accesssKey) // 사용할 암호화 알고리즘과
				// signature 에 들어갈 secret값 세팅
				.compact();

		return accessToken;
	}
}