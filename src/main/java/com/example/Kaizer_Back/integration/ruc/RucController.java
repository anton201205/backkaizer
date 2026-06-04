package com.example.Kaizer_Back.integration.ruc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Kaizer_Back.integration.ruc.dto.RucResponse;

@RestController
@RequestMapping("/api/consulta")
public class RucController {

	private final RucService rucService;

	public RucController(RucService rucService) {
		this.rucService = rucService;
	}

	@GetMapping("/ruc/{ruc}")
	public RucResponse consultarRuc(@PathVariable String ruc) {
		return rucService.consultar(ruc);
	}
}
