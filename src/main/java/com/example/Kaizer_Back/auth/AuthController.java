package com.example.Kaizer_Back.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.Kaizer_Back.auth.dto.AuthRequest;
import com.example.Kaizer_Back.auth.dto.AuthResponse;
import com.example.Kaizer_Back.usuario.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UsuarioService usuarioService;
	private final UsuarioDetailsService usuarioDetailsService;
	private final JwtService jwtService;

	public AuthController(
			AuthenticationManager authenticationManager,
			UsuarioService usuarioService,
			UsuarioDetailsService usuarioDetailsService,
			JwtService jwtService
	) {
		this.authenticationManager = authenticationManager;
		this.usuarioService = usuarioService;
		this.usuarioDetailsService = usuarioDetailsService;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody AuthRequest request) {
		usuarioService.registrar(request.email(), request.password());
		UserDetails userDetails = usuarioDetailsService.loadUserByUsername(request.email());
		return new AuthResponse(jwtService.generateToken(userDetails));
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody AuthRequest request) {
		var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());
		authenticationManager.authenticate(authToken);
		UserDetails userDetails = usuarioDetailsService.loadUserByUsername(request.email());
		return new AuthResponse(jwtService.generateToken(userDetails));
	}
}
