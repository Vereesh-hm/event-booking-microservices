package com.eventbooking.booking.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.eventbooking.booking.entity.BookingStatus;

public record BookingResponse(UUID bookingId, UUID userId, UUID eventId, Integer numberOfSeats,
		BookingStatus bookingStatus, LocalDateTime createdAt, EventDetailsResponse event) {
}