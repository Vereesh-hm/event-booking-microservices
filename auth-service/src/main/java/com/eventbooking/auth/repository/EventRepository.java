package com.eventbooking.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventbooking.auth.entity.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {

	Optional<Event> findByTitleAndLocationAndEventDate(String title, String location, LocalDateTime eventDate);
}
