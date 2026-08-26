package com.reviewer.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.reviewer.configuration.filter.JwtFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final JwtFilter jwtFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.formLogin(AbstractHttpConfigurer::disable)
				   .csrf(AbstractHttpConfigurer::disable)
				   .cors(Customizer.withDefaults())
				   .authorizeHttpRequests(requests -> {
					   requests.requestMatchers(HttpMethod.GET).permitAll();
					   requests.requestMatchers(HttpMethod.POST).permitAll();
					   requests.requestMatchers(HttpMethod.PATCH).permitAll();
					   requests.requestMatchers(HttpMethod.DELETE).permitAll();
					   
				   }).sessionManagement(manager ->
				                        manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				     .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				     .build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://192.168.219.103:443", "http://124.59.178.29:443"));
		configuration.setAllowedMethods(Arrays.asList("POST", "PATCH", "DELETE", "GET", "PUT", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
}
