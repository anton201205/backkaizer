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

	// rollbackFor = Exception.class garantiza rollback también ante excepciones checked
	// (ej. fallo de red al persistir), no solo RuntimeException como hace el default.
	@Transactional(rollbackFor = Exception.class)
public CheckoutResponse checkout(CheckoutRequest request, Long userId) {
    if (userId == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para completar la compra");
    }

    Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

    Pedido pedido = Pedido.builder()
            .estado("CREADO")
            .usuario(usuario)
            .build();

    String direccion = request.getDireccionEnvio() != null
            ? request.getDireccionEnvio()
            : usuario.getDireccion();
    pedido.setDireccionEnvio(direccion);

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

        producto.setStock(actual - quantity);

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

