package com.example.Kaizer_Back.producto;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Kaizer_Back.auth.UsuarioPrincipal;
import com.example.Kaizer_Back.producto.dto.EstadoRequest;
import com.example.Kaizer_Back.producto.dto.PedidoResponse;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;

	public PedidoController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	@GetMapping("/mis-pedidos")
	public List<PedidoResponse> misPedidos(@AuthenticationPrincipal UsuarioPrincipal principal) {
		return pedidoService.obtenerMisPedidos(principal.getId());
	}

	@GetMapping("/admin/todos")
	public List<PedidoResponse> todos() {
		return pedidoService.obtenerTodos();
	}

	@PatchMapping("/admin/{id}/estado")
	public ResponseEntity<PedidoResponse> actualizarEstado(
			@PathVariable Long id,
			@RequestBody EstadoRequest body) {
		try {
			return ResponseEntity.ok(pedidoService.actualizarEstado(id, body.estado()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}
}
