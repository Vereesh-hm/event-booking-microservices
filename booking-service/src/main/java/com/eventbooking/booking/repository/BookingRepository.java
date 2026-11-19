
package com.eventbooking.booking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eventbooking.booking.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

	List<Booking> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
