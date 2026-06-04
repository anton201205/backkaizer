package com.example.Kaizer_Back.usuario;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.Kaizer_Back.usuario.dto.UsuarioProfileRequest;
import com.example.Kaizer_Back.usuario.dto.UsuarioProfileResponse;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(rollbackFor = Exception.class)
	public Usuario registrar(String email, String password) {
		if (usuarioRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
		}

		Usuario usuario = Usuario.builder()
				.email(email)
				.passwordHash(passwordEncoder.encode(password))
				.role(Role.USER)
				.createdAt(OffsetDateTime.now())
				.build();

		return usuarioRepository.save(usuario);
	}

	@Transactional(rollbackFor = Exception.class)
	public UsuarioProfileResponse actualizarPerfil(Long userId, UsuarioProfileRequest request) {
		Usuario usuario = usuarioRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

		// Actualización parcial: solo se mutan los campos enviados en el request
		// para no borrar datos existentes cuando el cliente omite un campo.
		if (request.nombre() != null) usuario.setNombre(request.nombre());
		if (request.telefono() != null) usuario.setTelefono(request.telefono());
		if (request.direccion() != null) usuario.setDireccion(request.direccion());
		if (request.ciudad() != null) usuario.setCiudad(request.ciudad());

		return UsuarioProfileResponse.from(usuarioRepository.save(usuario));
	}
}
