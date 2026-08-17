package com.eventbooking.event.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.eventbooking.event.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DuplicateEventException.class)
	public ResponseEntity<ApiResponse<Void>> handleDuplicateEventException(DuplicateEventException ex) {

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.CONFLICT.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(EventNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleEventNotFoundException(EventNotFoundException ex) {

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.NOT_FOUND.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {

		log.warn("Invalid event id provided: {}", ex.getValue());

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), "Invalid event id", null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(InvalidEventQueryException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidEventQuery(InvalidEventQueryException ex) {

		log.warn("Invalid event query: {}", ex.getMessage());

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
			MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		log.warn("Validation failed: {}", errors);

		ApiResponse<Map<String, String>> response = new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(),
				"Validation failed", errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {

		log.warn("Malformed request body: {}", ex.getMessage());

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), "Invalid request body",
				null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

		log.error("Database constraint violation", ex);

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.CONFLICT.value(),
				"Database constraint violation", null);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(EventAlreadyExistsException.class)
	public ResponseEntity<ApiResponse<Void>> handleEventAlreadyExists(EventAlreadyExistsException ex) {

		log.warn("Duplicate event request: {}", ex.getMessage());

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.CONFLICT.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {

		log.error("Unexpected error occurred", ex);

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"An unexpected error occurred", null);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(EventAlreadyBookedException.class)
	public ResponseEntity<ApiResponse<Void>> handleEventAlreadyBooked(EventAlreadyBookedException ex) {

		log.warn("Event deletion conflict: {}", ex.getMessage());

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.CONFLICT.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

}
