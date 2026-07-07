package com.example.Kaizer_Back.usuario;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Kaizer_Back.auth.UsuarioPrincipal;
import com.example.Kaizer_Back.usuario.dto.UsuarioProfileRequest;
import com.example.Kaizer_Back.usuario.dto.UsuarioProfileResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil")
    public UsuarioProfileResponse obtenerPerfil(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return usuarioService.obtenerPerfil(principal.getId());
    }

    @PutMapping("/perfil")
    public UsuarioProfileResponse actualizarPerfil(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody UsuarioProfileRequest request) {
        return usuarioService.actualizarPerfil(principal.getId(), request);
    }
}