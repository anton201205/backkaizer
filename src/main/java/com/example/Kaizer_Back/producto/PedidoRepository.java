package com.example.Kaizer_Back.producto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	// JOIN FETCH en una sola consulta para evitar el problema N+1 al cargar items y productos.
	// DISTINCT elimina duplicados de Pedido que JPQL genera al hacer JOIN con una colección.
	@Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.items i JOIN FETCH i.producto WHERE p.usuario.id = :usuarioId ORDER BY p.createdAt DESC")
	List<Pedido> findByUsuarioIdWithItems(@Param("usuarioId") Long usuarioId);

	@Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.usuario JOIN FETCH p.items i JOIN FETCH i.producto ORDER BY p.createdAt DESC")
	List<Pedido> findAllWithItems();
}

