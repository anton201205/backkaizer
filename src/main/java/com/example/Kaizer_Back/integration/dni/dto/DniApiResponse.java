package com.example.Kaizer_Back.integration.dni.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DniApiResponse(
		@JsonProperty("first_name") String firstName,
		@JsonProperty("first_last_name") String firstLastName,
		@JsonProperty("second_last_name") String secondLastName,
		@JsonProperty("full_name") String fullName,
		@JsonProperty("document_number") String documentNumber
) {
}
