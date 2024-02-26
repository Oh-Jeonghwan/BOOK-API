package com.example.demo.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.example.demo.auth.exception.CustomAuthenticationEntryPoint;
import com.example.demo.auth.filter.JwtAuthenticationFilter;
import com.example.demo.auth.filter.JwtExceptionFilter;
import com.example.demo.auth.provider.JwtTokenProvider;
import com.example.demo.service.JwtService;

//보안 설정 클래스 작성 : 보안 구성을 위한 클래스 설정을 작성합니다.
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private JwtTokenProvider jwtTokenProvider;
    private JwtService jwtService;
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    
    public SecurityConfig(JwtTokenProvider jwtTokenProvider, JwtService jwtService, 
    					  CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.jwtService = jwtService;
		this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
	}

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    	http.cors().configurationSource(request -> {
    		CorsConfiguration cors = new CorsConfiguration().applyPermitDefaultValues();
    		// 환경별 분리 처리예정
    		//cors.setAllowedOrigins(List.of("http://localhost:3000", "http://192.168.1.112:8084"));
    		cors.setAllowedOrigins(List.of("*"));
            cors.setAllowedMethods(List.of("GET","POST", "PUT", "DELETE"));
            cors.setAllowedHeaders(List.of("*"));
    		return cors;
    	});
    	
        http
            .csrf().disable()
            .httpBasic().disable() // rest api만을 고려하여 기본 설정은 해제하겠음 http 방식은 header 에 세션을 생성할 인증 정보 ex) id pw 를 들고 그걸로 세션 생성 해주는 방식
            					   // 그래서 토큰 인증 방식인 bearer 방식을 사용할 것이다.
            .formLogin().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 토큰 기반 인증이므로 세션 역시 사용하지 않음
            .and()
//            .authorizeRequests(authorizeRequests ->
//                authorizeRequests
//                    .antMatchers("/api/public/**").permitAll()  // Public endpoints
//                    .anyRequest().authenticated()
//                    
//            )
            .authorizeRequests()
            .antMatchers("/book","/book/**")
            .hasAnyAuthority("MEMBER","ADMIN","APPROVE")
            .antMatchers("/", "/login", "/login/**","/join/**").permitAll()
            .and()
            //addFilterBefore이든 addFilterAfter이든 시큐리티 필터보다는 뒤에 동작한다. 
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, jwtService), UsernamePasswordAuthenticationFilter.class)  
            //JwtAuthenticationFilter를 UsernamePassworkdAuthenticationFilter 전에 넣는다.
            .addFilterBefore(new JwtExceptionFilter(), JwtAuthenticationFilter.class)
            .exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint)
            ;
            //.apply(new MyAuthenticationConfigurer());  // Apply custom authentication config

        return http.build();
    }

//    private static class MyAuthenticationConfigurer extends AbstractHttpConfigurer<MyAuthenticationConfigurer, HttpSecurity> {
//
//        @Override
//        public void configure(HttpSecurity http) throws Exception {
//            http
//                .formLogin()
//                    .loginPage("/login")  // Custom login page URL
//                    .permitAll();
//        }
//    }

    // Define other beans or methods as needed
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    
    @Bean
	protected AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    	//사용자가 로그인할 때, 입력한 자격 증명(credentials)을 검증하고 인증된 사용자 객체를 생성합니다.
		return authenticationConfiguration.getAuthenticationManager();
	}
}
