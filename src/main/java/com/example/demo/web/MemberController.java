package com.example.demo.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Account;
import com.example.demo.enumData.Roles;
import com.example.demo.service.AccountService;

import lombok.RequiredArgsConstructor;

@CrossOrigin("*")
@RequiredArgsConstructor
@RestController
public class MemberController {
	
	private final AccountService accountService;
	private final PasswordEncoder passwordEncoder;
	
	@PostMapping(value={"/join"})
	public ResponseEntity<?> joinProc(@RequestBody Account account) throws Exception {
		
		String encPassword = passwordEncoder.encode(account.getPassword());
		
		Account user = new Account();
		
		user.setLoginId(account.getLoginId());
		user.setPassword(encPassword);
		user.setRole(Roles.MEMBER);
		
		return new ResponseEntity<>(accountService.saveMember(user),HttpStatus.CREATED);
	}

}
