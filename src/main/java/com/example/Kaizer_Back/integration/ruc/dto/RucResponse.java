package com.example.Kaizer_Back.integration.ruc.dto;

public record RucResponse(
		String ruc,
		String razonSocial,
		String estado,
		String condicion,
		String direccionFiscal
) {
}
