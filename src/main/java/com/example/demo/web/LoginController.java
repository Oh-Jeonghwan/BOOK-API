package com.example.demo.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.auth.provider.JwtTokenProvider;
import com.example.demo.domain.Account;
import com.example.demo.dto.Token;
import com.example.demo.service.AccountService;
import com.example.demo.service.JwtService;

//@Api(value = "LoginController", tags = "로그인 관련 컨트롤러") //스웨거
@CrossOrigin("*")
@RestController
public class LoginController {
	
	private final PasswordEncoder passwordEncoder;
	private final AccountService accountService;
	private final JwtService jwtService;
	private JwtTokenProvider jwtTokenProvider;
	
	public LoginController(JwtTokenProvider jwtTokenProvider, JwtService jwtService,
			AccountService accountService, PasswordEncoder passwordEncoder) {
		this.accountService = accountService;
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
	}
	
	/**
	 * 로그인
	 */
	@PostMapping(value={"/login","/"})
	public ResponseEntity<?> login(@RequestBody Account user) throws Exception {
		Account account = accountService.findMember(user.getLoginId());

		Map<String, Object> retMap = new HashMap<String, Object>();
		
		if (account == null) {
			retMap.put("message", "ID를 다시 확인해주세요.");
			return new ResponseEntity<>(retMap, HttpStatus.BAD_REQUEST);
		} else if (!passwordEncoder.matches(user.getPassword(), account.getPassword())) {
			retMap.put("message", "비밀번호를 다시 확인해주세요.");
			return new ResponseEntity<>(retMap, HttpStatus.BAD_REQUEST);
		}

		UserDetails userInfo = accountService.loadUserByUsername(user.getLoginId());

		Token token = jwtTokenProvider.createToken(userInfo.getUsername(), account.getId(), userInfo.getAuthorities());

		jwtService.login(token);

		retMap.put("result", "OK");
		retMap.put("accountId", token.getAccountId());
		retMap.put("loginId", account.getLoginId());
		retMap.put("role", account.getRole());
		retMap.put("token", token.getAccessToken());
		retMap.put("refreshToken", token.getRefreshToken());
		//retMap.put("initPwAt", account.getInitPwAt()); // 초기 비밀번호 셋팅 여부

		return new ResponseEntity<>(retMap, HttpStatus.OK);
	}

	/**
	 * refreshToken
	 */
	@PostMapping("/login/refresh")
	public ResponseEntity<?> refreshToken(@RequestBody HashMap<String, String> map, ServletRequest request) {
		// refreshToken이 유효한지 확인한다.
		Map<String, Object> token = jwtService.validateRefreshToken(map.get("refreshToken"), request);

		Map<String, Object> retMap = new HashMap<>();
		retMap.put("token", token);
		if (token.get("code") == null) {
			return new ResponseEntity<>(retMap, HttpStatus.OK);
		}
		return new ResponseEntity<>(retMap, HttpStatus.BAD_REQUEST);
	}

}
