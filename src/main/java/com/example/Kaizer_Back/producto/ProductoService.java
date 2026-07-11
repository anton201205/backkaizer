package com.example.Kaizer_Back.producto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.Kaizer_Back.producto.dto.ProductoRequest;
import com.example.Kaizer_Back.producto.dto.ProductoResponse;

@Service
public class ProductoService {

	private final ProductoRepository productoRepository;

	public ProductoService(ProductoRepository productoRepository) {
		this.productoRepository = productoRepository;
	}

	@Transactional(rollbackFor = Exception.class)
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

	@Transactional(rollbackFor = Exception.class)
	public ProductoResponse actualizar(Long id, ProductoRequest request) {
		Producto producto = productoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

		producto.setNombre(request.nombre());
		producto.setDescripcion(request.descripcion());
		producto.setPrecio(request.precio());
		producto.setImageUrl(request.imageUrl());
		producto.setStock(request.stock());

		Producto saved = productoRepository.save(producto);
		return toResponse(saved);
	}

	@Transactional(rollbackFor = Exception.class)
	public void eliminar(Long id) {
		if (!productoRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
		}
		productoRepository.deleteById(id);
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