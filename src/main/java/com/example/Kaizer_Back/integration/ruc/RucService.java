package com.example.Kaizer_Back.integration.ruc;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.Kaizer_Back.integration.ruc.dto.RucApiResponse;
import com.example.Kaizer_Back.integration.ruc.dto.RucResponse;

@Service
public class RucService {

	private final RestClient decolectaClient;

	public RucService(RestClient decolectaClient) {
		this.decolectaClient = decolectaClient;
	}

	public RucResponse consultar(String ruc) {
		if (!ruc.matches("\\d{11}")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUC debe tener exactamente 11 dígitos");
		}

		RucApiResponse api = decolectaClient.get()
				.uri("/v1/sunat/ruc?numero={ruc}", ruc)
				.retrieve()
				.onStatus(status -> status.value() == 400 || status.value() == 422,
						// 422 es el código que retorna Decolecta para RUC con formato inválido o no encontrado
						(req, res) -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RUC no encontrado: " + ruc); })
				.onStatus(HttpStatusCode::is5xxServerError,
						(req, res) -> { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio SUNAT no disponible temporalmente"); })
				.body(RucApiResponse.class);

		// Compone la dirección fiscal concatenando los campos no nulos de menor a mayor nivel
		String direccionFiscal = Stream.of(api.direccion(), api.distrito(), api.provincia(), api.departamento())
				.filter(s -> s != null && !s.isBlank())
				.collect(Collectors.joining(", "));

		return new RucResponse(
				api.numeroDocumento(),
				api.razonSocial(),
				api.estado(),
				api.condicion(),
				direccionFiscal
		);
	}
}
