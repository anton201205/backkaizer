package com.example.Kaizer_Back.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	private final List<String> allowedOrigins;
	private final List<String> allowedOriginPatterns;

	public CorsConfig(@Value("${app.cors.allowed-origins}") String allowedOriginsCsv) {
		List<String> values = Arrays.stream(allowedOriginsCsv.split(","))
				.map(String::trim)
				.filter(v -> !v.isBlank())
				.toList();

		// Los orígenes con comodín (p. ej. https://*.vercel.app) deben ir como
		// allowedOriginPatterns; los exactos como allowedOrigins. Un patrón con
		// comodín es incompatible con allowCredentials si se usa como allowedOrigin.
		this.allowedOrigins = values.stream().filter(v -> !v.contains("*")).toList();
		this.allowedOriginPatterns = values.stream().filter(v -> v.contains("*")).toList();
	}

	/**
	 * Bean que Spring Security consume vía {@code http.cors(withDefaults())}.
	 * Definirlo como CorsConfigurationSource (y no como WebMvcConfigurer) garantiza
	 * que el CorsFilter de Security aplique estos orígenes también a los preflight
	 * (OPTIONS), evitando el 403 "Invalid CORS request".
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		if (!allowedOrigins.isEmpty()) {
			config.setAllowedOrigins(allowedOrigins);
		}
		if (!allowedOriginPatterns.isEmpty()) {
			config.setAllowedOriginPatterns(allowedOriginPatterns);
		}
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
