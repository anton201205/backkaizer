package com.example.Kaizer_Back.checkout;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.Kaizer_Back.auth.UsuarioPrincipal;
import com.example.Kaizer_Back.checkout.dto.CheckoutRequest;
import com.example.Kaizer_Back.checkout.dto.CheckoutResponse;

import jakarta.validation.Valid;

@RestController
public class CheckoutController {

	private final CheckoutService checkoutService;

	public CheckoutController(CheckoutService checkoutService) {
		this.checkoutService = checkoutService;
	}
@PostMapping("/api/checkout")
public CheckoutResponse checkout(
		@AuthenticationPrincipal UsuarioPrincipal principal,
		@Valid @RequestBody CheckoutRequest request) {
	return checkoutService.checkout(request, principal.getId());
}
}

