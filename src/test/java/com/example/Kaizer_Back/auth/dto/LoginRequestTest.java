package com.example.Kaizer_Back.auth.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

    @Test
    void shouldExposeEmailAndPassword() {
        LoginRequest request = new LoginRequest("user@example.com", "secret123");

        assertEquals("user@example.com", request.email());
        assertEquals("secret123", request.password());
    }
}
