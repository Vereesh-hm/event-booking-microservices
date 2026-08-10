package com.eventbooking.auth.service;

import com.eventbooking.auth.dto.ApiResponse;
import com.eventbooking.auth.dto.CreateEventRequest;
import com.eventbooking.auth.dto.EventResponse;

public interface EventService {

	ApiResponse<EventResponse> createEvent(CreateEventRequest createEventRequest);
}
