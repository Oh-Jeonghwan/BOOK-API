package com.example.demo.auth.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.GenericFilterBean;

import com.example.demo.enumData.TokenCode;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;

import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

public class JwtExceptionFilter extends GenericFilterBean{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		response.setCharacterEncoding("utf-8");
		
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
