package com.eventbooking.event.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.event.dto.ApiResponse;
import com.eventbooking.event.dto.CreateEventRequest;
import com.eventbooking.event.dto.EventPageResponse;
import com.eventbooking.event.dto.EventResponse;
import com.eventbooking.event.service.EventService;

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

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable("id") UUID id) {

		ApiResponse<EventResponse> response = eventService.getEventById(id);

		return ResponseEntity.status(response.getStatusCode()).body(response);

	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<EventPageResponse>> getAllEvents(
	        @RequestParam(name = "page", defaultValue = "0") int page,
	        @RequestParam(name = "size", defaultValue = "10") int size,
	        @RequestParam(name = "sortBy", defaultValue = "eventDate") String sortBy,
	        @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {

	    ApiResponse<EventPageResponse> response =
	            eventService.getAllEvents(page, size, sortBy, sortDirection);

	    return ResponseEntity
	            .status(response.getStatusCode())
	            .body(response);
	}
}
