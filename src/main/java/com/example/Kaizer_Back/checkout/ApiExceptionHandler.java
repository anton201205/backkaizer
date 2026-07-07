package com.example.Kaizer_Back.checkout;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(StockInsuficienteException.class)
	public ResponseEntity<String> handleStock(StockInsuficienteException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}

	// Maneja las violaciones de @Pattern, @NotBlank, etc. en path variables y params
@ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
public ResponseEntity<String> handleValidation(org.springframework.web.bind.MethodArgumentNotValidException ex) {
	String mensaje = ex.getBindingResult().getFieldErrors().stream()
			.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
			.findFirst()
			.orElse("Datos inválidos");
	return ResponseEntity.badRequest().body(mensaje);
}
}

