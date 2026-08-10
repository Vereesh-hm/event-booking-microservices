package com.eventbooking.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventResponse {

	private UUID id;

	private String title;

	private String description;

	private String location;

	private LocalDateTime eventDate;

	private Integer totalSeats;

	private Integer availableSeats;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;
}
