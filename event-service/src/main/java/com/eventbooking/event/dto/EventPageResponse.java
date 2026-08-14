package com.eventbooking.event.dto;

import java.util.List;

public record EventPageResponse(List<EventResponse> events, int page, int size, long totalElements, int totalPages) {
}