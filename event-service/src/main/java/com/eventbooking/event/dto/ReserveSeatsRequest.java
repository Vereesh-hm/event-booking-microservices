package com.eventbooking.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReserveSeatsRequest {

	@NotNull(message = "Number of seats is required")
	@Min(value = 1, message = "At least one seat must be reserved")
	private Integer numberOfSeats;
}