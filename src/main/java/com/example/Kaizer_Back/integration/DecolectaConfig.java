package com.example.Kaizer_Back.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DecolectaConfig {

	@Bean
	public RestClient decolectaClient(
			@Value("${app.integration.decolecta.url}") String url,
			@Value("${app.integration.decolecta.token}") String token) {
		// Un solo cliente compartido para DNI (RENIEC) y RUC (SUNAT) ya que ambos
		// usan la misma base URL y el mismo token de autenticación.
		return RestClient.builder()
				.baseUrl(url)
				.defaultHeader("Authorization", "Bearer " + token)
				.defaultHeader("Content-Type", "application/json")
				.build();
	}
}
