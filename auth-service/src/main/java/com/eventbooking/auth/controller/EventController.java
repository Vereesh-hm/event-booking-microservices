package com.eventbooking.auth.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.auth.dto.ApiResponse;
import com.eventbooking.auth.dto.CreateEventRequest;
import com.eventbooking.auth.dto.EventResponse;
import com.eventbooking.auth.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventController {

	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@PostMapping(value = "/createEvent", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<EventResponse>> createEvent(
			@Valid @RequestBody CreateEventRequest createEventRequest) {

		ApiResponse<EventResponse> response = eventService.createEvent(createEventRequest);

		return ResponseEntity.status(response.getStatusCode()).body(response);
	}
}
