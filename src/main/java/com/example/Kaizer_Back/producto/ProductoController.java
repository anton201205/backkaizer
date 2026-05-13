package com.example.Kaizer_Back.producto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.Kaizer_Back.producto.dto.ProductoRequest;
import com.example.Kaizer_Back.producto.dto.ProductoResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	private final ProductoService productoService;

	public ProductoController(ProductoService productoService) {
		this.productoService = productoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
		return productoService.crear(request);
	}

	@GetMapping
	public List<ProductoResponse> listar() {
		return productoService.listar();
	}

	@GetMapping("/{id}")
	public ProductoResponse obtener(@PathVariable Long id) {
		return productoService.obtener(id);
	}
}
