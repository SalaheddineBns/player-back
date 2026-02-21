package com.salah.mcpplayersservice.exceptions;

import com.salah.mcpplayersservice.dto.response.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(TeamAlreadyExistsException.class)
	public ResponseEntity<ErrorResponseDto> handleTeamAlreadyExist(TeamAlreadyExistsException ex) {
		ErrorResponseDto error = new ErrorResponseDto(HttpStatus.CONFLICT.value(), ex.getMessage(),
				System.currentTimeMillis());
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(PlayerAlreadyExistsException.class)
	public ResponseEntity<ErrorResponseDto> handlePlayerAlreadyExists(PlayerAlreadyExistsException ex) {
		ErrorResponseDto error = new ErrorResponseDto(HttpStatus.CONFLICT.value(), ex.getMessage(),
				System.currentTimeMillis());
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(UserAlreadyExistsException ex) {
		ErrorResponseDto error = new ErrorResponseDto(HttpStatus.CONFLICT.value(), ex.getMessage(),
				System.currentTimeMillis());
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex) {
		ErrorResponseDto error = new ErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password",
				System.currentTimeMillis());
		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponseDto> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
		ErrorResponseDto error = new ErrorResponseDto(HttpStatus.PAYLOAD_TOO_LARGE.value(),
				"File size exceeds the maximum allowed limit", System.currentTimeMillis());
		return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(e -> e.getDefaultMessage())
			.findFirst()
			.orElse("Validation failed");
		ErrorResponseDto error = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), message,
				System.currentTimeMillis());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

}
