package com.example.demo.dto;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.example.demo.domain.Account;

public class UserAccount extends User {
	
	private static final long serialVersionUID = 4303000141644866980L;
	
	private Account account;

	public UserAccount(Account account, Collection<? extends GrantedAuthority> authorities) {
			//String username, String password, Collection<? extends GrantedAuthority> authorities
			
		super(account.getLoginId(), account.getPassword(), authorities);
		this.account = account;
	}

	
}
