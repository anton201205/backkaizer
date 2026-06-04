package com.example.Kaizer_Back.checkout;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.Kaizer_Back.checkout.dto.CheckoutRequest;
import com.example.Kaizer_Back.checkout.dto.CheckoutResponse;
import com.example.Kaizer_Back.producto.Pedido;
import com.example.Kaizer_Back.producto.PedidoItem;
import com.example.Kaizer_Back.producto.PedidoRepository;
import com.example.Kaizer_Back.producto.Producto;
import com.example.Kaizer_Back.producto.ProductoRepository;
import com.example.Kaizer_Back.usuario.Usuario;
import com.example.Kaizer_Back.usuario.UsuarioRepository;

@Service
public class CheckoutService {

	private final ProductoRepository productoRepository;
	private final PedidoRepository pedidoRepository;
	private final UsuarioRepository usuarioRepository;

	public CheckoutService(ProductoRepository productoRepository, PedidoRepository pedidoRepository,
			UsuarioRepository usuarioRepository) {
		this.productoRepository = productoRepository;
		this.pedidoRepository = pedidoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public CheckoutResponse checkout(CheckoutRequest request, Long userId) {
		// Crea pedido (orden) y aplica descuento de stock con bloqueo pesimista.
		// Esto mitiga race conditions cuando múltiples compras intentan tomar el mismo stock.
		Pedido pedido = Pedido.builder().estado("CREADO").build();

		if (userId != null) {
			// findById en lugar de getReferenceById porque necesitamos los datos del perfil
			// para generar el snapshot de facturación (nombre, teléfono, dirección).
			Usuario usuario = usuarioRepository.findById(userId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

			pedido.setUsuario(usuario);
			pedido.setNombreComprador(usuario.getNombre());
			pedido.setTelefonoComprador(usuario.getTelefono());

			// Dirección: prioriza lo enviado en el request; si no viene, usa la del perfil.
			String direccion = request.getDireccionEnvio() != null
					? request.getDireccionEnvio()
					: usuario.getDireccion();
			pedido.setDireccionEnvio(direccion);
		} else if (request.getDireccionEnvio() != null) {
			pedido.setDireccionEnvio(request.getDireccionEnvio());
		}

		BigDecimal total = BigDecimal.ZERO;

		for (CheckoutRequest.Item item : request.getItems()) {
			Long productId = item.getProductId();
			int quantity = item.getQuantity();

			Producto producto = productoRepository.findByIdForUpdate(productId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe: " + productId));

			int actual = producto.getStock() == null ? 0 : producto.getStock();
			if (actual < quantity) {
				throw new StockInsuficienteException(productId, actual, quantity);
			}

			// Descontar stock
			producto.setStock(actual - quantity);

			// Registrar item
			PedidoItem pedidoItem = PedidoItem.builder()
					.pedido(pedido)
					.producto(producto)
					.cantidad(quantity)
					.precioUnitario(producto.getPrecio())
					.build();

			pedido.getItems().add(pedidoItem);

			total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(quantity)));
		}

		pedido.setTotal(total);

		Pedido saved = pedidoRepository.save(pedido);
		return new CheckoutResponse(saved.getId(), saved.getTotal());
	}
}

