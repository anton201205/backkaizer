package com.example.Kaizer_Back.checkout.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CheckoutRequest {

	@NotEmpty
	private List<Item> items;

	// Dirección de envío ingresada en el formulario de checkout.
	// Si no se envía, el servicio usa la guardada en el perfil del usuario.
	@Size(max = 255)
	private String direccionEnvio;

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public String getDireccionEnvio() {
		return direccionEnvio;
	}

	public void setDireccionEnvio(String direccionEnvio) {
		this.direccionEnvio = direccionEnvio;
	}

	public static class Item {
		@NotNull
		private Long productId;

		@Positive
		private Integer quantity;

		public Long getProductId() {
			return productId;
		}

		public void setProductId(Long productId) {
			this.productId = productId;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public void setQuantity(Integer quantity) {
			this.quantity = quantity;
		}
	}
}

