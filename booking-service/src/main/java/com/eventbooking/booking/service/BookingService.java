package com.eventbooking.booking.service;

import java.util.List;
import java.util.UUID;

import com.eventbooking.booking.dto.request.BookingRequest;
import com.eventbooking.booking.dto.response.BookingCancellationResponse;
import com.eventbooking.booking.dto.response.BookingResponse;

public interface BookingService {

	BookingResponse createBooking(BookingRequest request, String authenticatedUserId, String authorizationHeader);

	BookingResponse getBookingById(UUID bookingId, String authenticatedUserId, String authorizationHeader);

	List<BookingResponse> getMyBookings(
	        String authenticatedUserId,
	        String authorizationHeader
	);
	
	BookingCancellationResponse cancelBooking(
	        UUID bookingId,
	        String authenticatedUserId,
	        String authorizationHeader
	);
}