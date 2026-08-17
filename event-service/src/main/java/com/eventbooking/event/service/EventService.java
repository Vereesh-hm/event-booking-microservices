package com.eventbooking.event.service;

import java.util.UUID;

import com.eventbooking.event.dto.ApiResponse;
import com.eventbooking.event.dto.CreateEventRequest;
import com.eventbooking.event.dto.EventPageResponse;
import com.eventbooking.event.dto.EventResponse;
import com.eventbooking.event.dto.UpdateEventRequest;

public interface EventService {

	ApiResponse<EventResponse> createEvent(CreateEventRequest createEventRequest);

	ApiResponse<EventResponse> getEventById(UUID id);

	ApiResponse<EventPageResponse> getAllEvents(int page, int size, String sortBy, String sortDirection);

	ApiResponse<EventResponse> updateEvent(UUID id, UpdateEventRequest request);

	ApiResponse<Void> deleteEvent(UUID id);
}