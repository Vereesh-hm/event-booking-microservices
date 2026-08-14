package com.eventbooking.event.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.NOT_FOUND.value(),
				"Event details not found for the given id", null);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(InvalidEventQueryException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidEventQuery(InvalidEventQueryException ex) {

		log.warn("Invalid event query: {}", ex.getMessage());

		ApiResponse<Void> response = new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

}
