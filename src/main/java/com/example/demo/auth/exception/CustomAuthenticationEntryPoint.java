package com.example.demo.auth.exception;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
//import org.json.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.demo.enumData.TokenCode;

@Component //스프링이 해당 클래스를 빈으로 인식하도록 하세요.
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint{

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		Object exception = request.getAttribute("exception");

		// 잘못된 타입의 토큰인 경우
		if (exception.equals(TokenCode.WRONG_TYPE_TOKEN.getCode())) {
			setResponse(response, TokenCode.WRONG_TYPE_TOKEN);
		} else if (exception.equals(TokenCode.EXPIRED_TOKEN.getCode())) {
			// 토큰 만료된 경우
			setResponse(response, TokenCode.EXPIRED_TOKEN);
		} else if (exception.equals(TokenCode.UNSUPPORTED_TOKEN.getCode())) {
			// 지원하지 않는 토큰인 경우
			setResponse(response, TokenCode.UNSUPPORTED_TOKEN);
		} else if (exception.equals(TokenCode.ACCESS_DENIED.getCode())) {
			setResponse(response, TokenCode.ACCESS_DENIED);
		} else {
			setResponse(response, TokenCode.UNKNOWN_ERROR);
		}
	}

	// 한글 출력을 위해 getWriter() 사용
	private void setResponse(HttpServletResponse response, TokenCode code) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		JSONObject responseJson = new JSONObject();
		responseJson.put("message", code.getMessage());
		responseJson.put("code", code.getCode());

		response.getWriter().print(responseJson);
	}

}
