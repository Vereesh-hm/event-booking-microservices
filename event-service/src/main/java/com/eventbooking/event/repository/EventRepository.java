package com.eventbooking.event.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eventbooking.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {

	Optional<Event> findByTitleAndLocationAndEventDate(String title, String location, LocalDateTime eventDate);

	@Modifying
	@Query("""
			    UPDATE Event e
			    SET e.availableSeats = e.availableSeats - :numberOfSeats
			    WHERE e.id = :eventId
			    AND e.availableSeats >= :numberOfSeats
			""")
	int reserveSeats(@Param("eventId") UUID eventId, @Param("numberOfSeats") Integer numberOfSeats);

	@Modifying
	@Query("""
			    UPDATE Event e
			    SET e.availableSeats = e.availableSeats + :numberOfSeats
			    WHERE e.id = :eventId
			    AND e.availableSeats + :numberOfSeats <= e.totalSeats
			""")
	int releaseSeats(@Param("eventId") UUID eventId, @Param("numberOfSeats") Integer numberOfSeats);
}
