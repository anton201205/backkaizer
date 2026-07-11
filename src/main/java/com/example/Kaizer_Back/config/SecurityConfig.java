package com.example.Kaizer_Back.config;

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

import com.example.Kaizer_Back.auth.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
			throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults()) // Usa el bean CorsConfig definido en config/CorsConfig.java
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/health").permitAll()
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/checkout").authenticated()
						// Consultas públicas para autocompletado en registro y checkout
						.requestMatchers(HttpMethod.GET, "/api/consulta/dni/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/consulta/ruc/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/pedidos/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/pedidos/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
		
	}
}