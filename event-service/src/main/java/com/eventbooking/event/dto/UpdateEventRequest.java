package com.eventbooking.event.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateEventRequest(

		@NotBlank(message = "Title is required") @Size(max = 200, message = "Title must not exceed 200 characters") String title,

		@Size(max = 1000, message = "Description must not exceed 1000 characters") String description,

		@NotBlank(message = "Location is required") @Size(max = 200, message = "Location must not exceed 200 characters") String location,

		@Future(message = "Event date must be in the future") @NotNull(message = "Event date is required") LocalDateTime eventDate,

		@Positive(message = "Total seats must be greater than zero") int totalSeats) {
}