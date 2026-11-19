package com.eventbooking.booking.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EventDetailsResponse {

    private String title;
    private String description;
    private String location;
    private LocalDateTime eventDate;
}