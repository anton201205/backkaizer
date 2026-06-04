package com.example.Kaizer_Back.integration.dni;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.Kaizer_Back.integration.dni.dto.DniApiResponse;
import com.example.Kaizer_Back.integration.dni.dto.DniResponse;

@Service
public class DniService {

	private final RestClient decolectaClient;

	public DniService(RestClient decolectaClient) {
		this.decolectaClient = decolectaClient;
	}

	public DniResponse consultar(String dni) {
		if (!dni.matches("\\d{8}")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI debe tener exactamente 8 dígitos");
		}

		DniApiResponse api = decolectaClient.get()
				.uri("/v1/reniec/dni?numero={dni}", dni)
				.retrieve()
				.onStatus(status -> status.value() == 400,
						// La API devuelve 400 cuando el DNI no existe en RENIEC
						(req, res) -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DNI no encontrado: " + dni); })
				.onStatus(HttpStatusCode::is5xxServerError,
						(req, res) -> { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio RENIEC no disponible temporalmente"); })
				.body(DniApiResponse.class);

		return new DniResponse(
				api.documentNumber(),
				api.fullName()
		);
	}
}
