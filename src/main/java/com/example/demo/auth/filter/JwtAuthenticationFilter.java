package com.example.demo.auth.filter;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.example.demo.auth.provider.JwtTokenProvider;
import com.example.demo.enumData.TokenCode;
import com.example.demo.service.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

//public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
//
//  @Override
//  public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//          throws IOException, ServletException {
//      Authentication authentication = JwtTokenUtil.parseTokenAndCreateAuthentication(request);
//
//      if (authentication != null) {
//          SecurityContextHolder.getContext().setAuthentication(authentication);
//      }
//
//      chain.doFilter(request, response);
//  }
//}


public class JwtAuthenticationFilter extends GenericFilterBean{ //UsernamePasswordAuthenticationFilter
	
	private JwtTokenProvider jwtTokenProvider;
	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, JwtService jwtService) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtService = jwtService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);
		if (token != null && jwtTokenProvider.validateToken(token, request)) {
			if (jwtService.getAccessToken(token).isEmpty()) {
				request.setAttribute("exception", TokenCode.EXPIRED_TOKEN.getCode());
			} else {
				UsernamePasswordAuthenticationToken authentication = jwtTokenProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		try {
			chain.doFilter(request, response);
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
	}
}
