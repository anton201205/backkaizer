package com.example.Kaizer_Back.usuario;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

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
}
