package com.example.Kaizer_Back.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
		@NotBlank @Size(min = 2, max = 100) String nombre,
		@Email @NotBlank String email,
		@NotBlank @Size(min = 8, max = 100) String password
) {
}