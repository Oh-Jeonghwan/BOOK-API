package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
	Optional<RefreshToken> findByRefreshToken(String refreshToken);
	   
	Optional<RefreshToken> findByAccessToken(String refreshToken);
	   
	RefreshToken findByLoginId(String loginId);
	   
	boolean existsByLoginId(String loginId);
	   
	void deleteByLoginId(String loginId);
}
