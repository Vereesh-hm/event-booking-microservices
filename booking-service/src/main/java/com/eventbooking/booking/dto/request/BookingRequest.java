
package com.eventbooking.booking.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

	@NotNull(message = "Event ID is required")
	private UUID eventId;

	@NotNull(message = "Number of seats is required")
	@Min(value = 1, message = "At least one seat must be booked")
	private Integer numberOfSeats;
}
