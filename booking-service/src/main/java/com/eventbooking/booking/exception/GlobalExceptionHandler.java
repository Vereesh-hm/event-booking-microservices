
package com.eventbooking.booking.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.eventbooking.booking.dto.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private ResponseEntity<ApiResponse<Void>> buildErrorResponse(HttpStatus status, String message) {

		ApiResponse<Void> errorResponse = new ApiResponse<>(false, status.value(), message, null);

		return ResponseEntity.status(status).body(errorResponse);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidRequestBody(HttpMessageNotReadableException ex) {

		String message = "Invalid request body. Please provide valid field values.";

		if (ex.getMessage() != null && ex.getMessage().contains("UUID")) {
			message = "Invalid event ID format. Please provide a valid UUID.";
		}

		return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {

		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

		String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();

		return buildErrorResponse(status, message);
	}

	@ExceptionHandler(BookingNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleBookingNotFound(BookingNotFoundException ex) {

		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(BookingAccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleBookingAccessDenied(BookingAccessDeniedException ex) {

		return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(BookingAlreadyCancelledException.class)
	public ResponseEntity<ApiResponse<Void>> handleBookingAlreadyCancelled(BookingAlreadyCancelledException ex) {

		return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {

		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).findFirst()
				.orElse("Invalid request");

		return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(NoResourceFoundException ex) {

		return buildErrorResponse(HttpStatus.NOT_FOUND, "Requested API endpoint not found");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {

		log.error("Unexpected error occurred", ex);

		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred");
	}
}
