package com.salah.mcpplayersservice.exceptions;

public class RessourceNotFoundException extends RuntimeException {

	public RessourceNotFoundException(String resource, String field, Object value) {
		super(String.format("resource %s not found with %s: %s", resource, field, value));
	}

}
