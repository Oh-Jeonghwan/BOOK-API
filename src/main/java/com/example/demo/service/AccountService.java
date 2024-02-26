package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.Account;
import com.example.demo.dto.UserAccount;
import com.example.demo.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService{
	
	@Autowired
	private AccountRepository accountRepository;
	
	public Account saveMember(Account account) {
		return accountRepository.save(account);
	}
	
	@Override
	public UserDetails loadUserByUsername(String loginid) throws UsernameNotFoundException {
 		Account account = accountRepository.findByLoginId(loginid);
	
	    if(account == null) {
	        throw new UsernameNotFoundException(loginid);
	    }
	
	    List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(account.getRole().name()));
		
		//반환 값은 UserDetails인데 UserAccount를 반환해도 되는 이유 UserAccount dto가 User를 extends 하고 있기 때문에
		return new UserAccount(account, authorities);
	}
	
	public Optional<Account> findMember(Long accountId) {
		return accountRepository.findById(accountId);
	}
	
	public Account findMember(String loginId) {
		return accountRepository.findByLoginId(loginId);
	}

}
