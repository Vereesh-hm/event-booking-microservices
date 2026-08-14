package com.eventbooking.event.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventbooking.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {

	Optional<Event> findByTitleAndLocationAndEventDate(String title, String location, LocalDateTime eventDate);
}
