package com.example.Kaizer_Back.usuario;

import java.time.OffsetDateTime;
import jakarta.persistence.*; // Simplificamos los imports
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(length = 100)
    private String nombre;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Esto asigna la fecha automáticamente antes de insertar en la DB
    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        if (this.role == null) {
            this.role = Role.USER; // Valor por defecto si viene nulo
        }
    }
}