package com.example.Kaizer_Back.usuario.dto;

import com.example.Kaizer_Back.usuario.Usuario;

public record UsuarioProfileResponse(
		Long id,
		String email,
		String nombre,
		String telefono,
		String direccion,
		String distrito,
		String dni
) {
	public static UsuarioProfileResponse from(Usuario u) {
		return new UsuarioProfileResponse(
				u.getId(),
				u.getEmail(),
				u.getNombre(),
				u.getTelefono(),
				u.getDireccion(),
				u.getDistrito(),
				u.getDni()
		);
	}
}
