package com.example.Kaizer_Back.producto.dto;

import java.math.BigDecimal;

public record ProductoResponse(
		Long id,
		String nombre,
		String descripcion,
		BigDecimal precio,
		String imageUrl,
		Integer stock
) {
}
