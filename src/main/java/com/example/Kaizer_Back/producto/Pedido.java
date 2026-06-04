package com.example.Kaizer_Back.producto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.Kaizer_Back.usuario.Usuario;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "direccion_envio", columnDefinition = "TEXT")
    private String direccionEnvio;

    // Snapshot del nombre y teléfono al momento de la compra para que los datos
    // de facturación no cambien si el usuario actualiza su perfil después.
    @Column(name = "nombre_comprador", length = 100)
    private String nombreComprador;

    @Column(name = "telefono_comprador", length = 20)
    private String telefonoComprador;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PedidoItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        if (this.estado == null) {
            this.estado = "CREADO";
        }
        if (this.total == null) {
            this.total = BigDecimal.ZERO;
        }
    }
}