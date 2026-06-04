package com.example.Kaizer_Back.integration.dni;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Kaizer_Back.integration.dni.dto.DniResponse;

@RestController
@RequestMapping("/api/consulta")
public class DniController {

	private final DniService dniService;

	public DniController(DniService dniService) {
		this.dniService = dniService;
	}

	@GetMapping("/dni/{dni}")
	public DniResponse consultarDni(@PathVariable String dni) {
		return dniService.consultar(dni);
	}
}
