package com.eventbooking.auth.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "events")
@Data
public class Event {

	@Id
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
