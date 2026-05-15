package com.example.Kaizer_Back.producto;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "productos")
public class Producto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false, length = 2000)
	private String descripcion;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal precio;

	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	@Column(nullable = false)
	private Integer stock;
}
