package com.example.Kaizer_Back.producto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.Kaizer_Back.producto.dto.ProductoRequest;
import com.example.Kaizer_Back.producto.dto.ProductoResponse;

@Service
public class ProductoService {

	private final ProductoRepository productoRepository;

	public ProductoService(ProductoRepository productoRepository) {
		this.productoRepository = productoRepository;
	}

	public ProductoResponse crear(ProductoRequest request) {
		Producto producto = Producto.builder()
				.nombre(request.nombre())
				.descripcion(request.descripcion())
				.precio(request.precio())
				.imageUrl(request.imageUrl())
				.stock(request.stock())
				.build();

		Producto saved = productoRepository.save(producto);
		return toResponse(saved);
	}

	public List<ProductoResponse> listar() {
		return productoRepository.findAll().stream().map(this::toResponse).toList();
	}

	public ProductoResponse obtener(Long id) {
		return productoRepository.findById(id).map(this::toResponse)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
	}

	private ProductoResponse toResponse(Producto producto) {
		return new ProductoResponse(
				producto.getId(),
				producto.getNombre(),
				producto.getDescripcion(),
				producto.getPrecio(),
				producto.getImageUrl(),
				producto.getStock()
		);
	}
}
