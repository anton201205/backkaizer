package com.example.Kaizer_Back.integration.ruc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RucApiResponse(
		@JsonProperty("razon_social") String razonSocial,
		@JsonProperty("numero_documento") String numeroDocumento,
		String estado,
		String condicion,
		String direccion,
		String distrito,
		String provincia,
		String departamento
) {
}
