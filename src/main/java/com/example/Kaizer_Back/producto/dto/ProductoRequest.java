package com.example.Kaizer_Back.producto.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
		@NotBlank String nombre,
		@NotBlank String descripcion,
		@NotNull @Positive BigDecimal precio,
		@NotBlank @Size(max = 500) String imageUrl,
		@NotNull @PositiveOrZero Integer stock
) {
}
