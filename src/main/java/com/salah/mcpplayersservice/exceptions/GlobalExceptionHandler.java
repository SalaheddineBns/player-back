package com.salah.mcpplayersservice.exceptions;

import com.salah.mcpplayersservice.dto.response.ErrorResponseDto;
import org.springframework.dao.DataIntegrityViolationException;
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

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
		ErrorResponseDto error = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), ex.getMessage(),
				System.currentTimeMillis());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException ex) {
		String message = ex.getMostSpecificCause().getMessage();
		if (message != null && message.contains("duplicate key")) {
			ErrorResponseDto error = new ErrorResponseDto(HttpStatus.CONFLICT.value(),
					"A record with this value already exists", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.CONFLICT);
		}
		ErrorResponseDto error = new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"A database error occurred", System.currentTimeMillis());
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
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
