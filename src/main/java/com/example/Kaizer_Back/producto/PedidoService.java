package com.example.Kaizer_Back.producto;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Kaizer_Back.producto.dto.PedidoResponse;

@Service
public class PedidoService {

	private static final Set<String> ESTADOS_VALIDOS = Set.of("CREADO", "PROCESANDO", "ENVIADO", "ENTREGADO", "CANCELADO");

	private final PedidoRepository pedidoRepository;

	public PedidoService(PedidoRepository pedidoRepository) {
		this.pedidoRepository = pedidoRepository;
	}

	@Transactional(readOnly = true)
	public List<PedidoResponse> obtenerMisPedidos(Long usuarioId) {
		return pedidoRepository.findByUsuarioIdWithItems(usuarioId)
				.stream()
				.map(PedidoResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<PedidoResponse> obtenerTodos() {
		return pedidoRepository.findAllWithItems()
				.stream()
				.map(PedidoResponse::from)
				.toList();
	}

	@Transactional
	public PedidoResponse actualizarEstado(Long pedidoId, String nuevoEstado) {
		String estado = nuevoEstado.toUpperCase();
		if (!ESTADOS_VALIDOS.contains(estado)) {
			throw new IllegalArgumentException("Estado no válido: " + nuevoEstado);
		}
		Pedido pedido = pedidoRepository.findById(pedidoId)
				.orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + pedidoId));
		pedido.setEstado(estado);
		return PedidoResponse.from(pedidoRepository.save(pedido));
	}
}
