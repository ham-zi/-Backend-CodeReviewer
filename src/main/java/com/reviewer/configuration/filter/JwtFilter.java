package com.reviewer.configuration.filter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.common.token.Util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
	
	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailService;

	// 요 안에서 반환하는 경로들은 필터를 아예 안타게해주는 메소드
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String uri = request.getRequestURI();
		return uri.equals("/api/auth/login") || uri.equals("/api/auth/refresh");
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		
		if(authorization == null || !authorization.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String token = authorization.substring(7);
		try {
			Claims claims = jwtUtil.parseJwt(token);
			String username = claims.getSubject();
			
			CustomUserDetails user = (CustomUserDetails)userDetailService.loadUserByUsername(username);
			UsernamePasswordAuthenticationToken authentication
				= new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
		} catch(ExpiredJwtException e) {
			response.setStatus(401);
			response.setContentType("application/json; charset=UTF-8");
			response.getWriter().write("토큰만료");
			return;
			
		} catch(JwtException e) {
			response.setStatus(401);
			response.setContentType("application/json; charset=UTF-8");
			response.getWriter().write("유효하지않은토큰");
			return;
			
		}
		
		filterChain.doFilter(request, response);
	}
	
}