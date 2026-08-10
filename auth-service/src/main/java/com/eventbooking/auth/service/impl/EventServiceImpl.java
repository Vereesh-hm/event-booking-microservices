package com.eventbooking.auth.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.eventbooking.auth.dto.ApiResponse;
import com.eventbooking.auth.dto.CreateEventRequest;
import com.eventbooking.auth.dto.EventResponse;
import com.eventbooking.auth.entity.Event;
import com.eventbooking.auth.exception.DuplicateEventException;
import com.eventbooking.auth.repository.EventRepository;
import com.eventbooking.auth.service.EventService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

	private final EventRepository eventRepository;

	public EventServiceImpl(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	@Transactional
	public ApiResponse<EventResponse> createEvent(CreateEventRequest createEventRequest) {

	    log.info("Event creation request received. title={}, location={}, eventDate={}",
	            createEventRequest.getTitle(),
	            createEventRequest.getLocation(),
	            createEventRequest.getEventDate());

	    if (eventRepository.findByTitleAndLocationAndEventDate(
	            createEventRequest.getTitle(),
	            createEventRequest.getLocation(),
	            createEventRequest.getEventDate()).isPresent()) {

	        log.warn("Duplicate event creation attempt. title={}, location={}, eventDate={}",
	                createEventRequest.getTitle(),
	                createEventRequest.getLocation(),
	                createEventRequest.getEventDate());

	        throw new DuplicateEventException(
	                "An event with the same title, location and date already exists");
	    }

	    Event event = new Event();

	    event.setId(UUID.randomUUID());
	    event.setDescription(createEventRequest.getDescription());
	    event.setLocation(createEventRequest.getLocation());
	    event.setTitle(createEventRequest.getTitle());
	    event.setEventDate(createEventRequest.getEventDate());
	    event.setTotalSeats(createEventRequest.getTotalSeats());

	    // Initially all seats are available
	    event.setAvailableSeats(createEventRequest.getTotalSeats());

	    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

	    event.setCreatedAt(now);
	    event.setUpdatedAt(now);

	    Event savedEvent = eventRepository.save(event);

	    log.info("Event created successfully. eventId={}, title={}",
	            savedEvent.getId(),
	            savedEvent.getTitle());

	    EventResponse eventResponse = new EventResponse(
	            savedEvent.getId(),
	            savedEvent.getTitle(),
	            savedEvent.getDescription(),
	            savedEvent.getLocation(),
	            savedEvent.getEventDate(),
	            savedEvent.getTotalSeats(),
	            savedEvent.getAvailableSeats(),
	            savedEvent.getCreatedAt(),
	            savedEvent.getUpdatedAt());

	    return new ApiResponse<>(
	            true,
	            HttpStatus.CREATED.value(),
	            "Event created successfully",
	            eventResponse);
	}

}
