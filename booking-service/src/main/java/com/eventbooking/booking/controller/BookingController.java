package com.eventbooking.booking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.booking.dto.request.BookingRequest;
import com.eventbooking.booking.dto.response.BookingCancellationResponse;
import com.eventbooking.booking.dto.response.BookingResponse;
import com.eventbooking.booking.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

	private final BookingService bookingService;

	// API for book the event
	@PostMapping
	public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request,
			Authentication authentication, @RequestHeader("Authorization") String authorizationHeader) {

		BookingResponse response = bookingService.createBooking(request, authentication.getName(), authorizationHeader);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<BookingResponse> getBookingById(@PathVariable("bookingId") UUID bookingId,
			Authentication authentication, @RequestHeader("Authorization") String authorizationHeader) {

		BookingResponse response = bookingService.getBookingById(bookingId, authentication.getName(),
				authorizationHeader);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/my-bookings") // getAllBookings for particular user
	public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication authentication,
			@RequestHeader("Authorization") String authorizationHeader) {

		List<BookingResponse> response = bookingService.getMyBookings(authentication.getName(), authorizationHeader);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{bookingId}/cancel")
	public ResponseEntity<BookingCancellationResponse> cancelBooking(@PathVariable("bookingId") UUID bookingId,
			Authentication authentication, @RequestHeader("Authorization") String authorizationHeader) {

		BookingCancellationResponse response = bookingService.cancelBooking(bookingId, authentication.getName(),
				authorizationHeader);

		return ResponseEntity.ok(response);
	}

}