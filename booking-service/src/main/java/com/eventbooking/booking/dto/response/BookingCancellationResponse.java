package com.eventbooking.booking.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.eventbooking.booking.entity.BookingStatus;

public record BookingCancellationResponse(
        UUID bookingId,
        BookingStatus bookingStatus,
        LocalDateTime updatedAt,
        String message
) {}