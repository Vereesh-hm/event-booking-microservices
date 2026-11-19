
package com.eventbooking.booking.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.eventbooking.booking.client.EventServiceClient;
import com.eventbooking.booking.dto.request.BookingRequest;
import com.eventbooking.booking.dto.response.BookingCancellationResponse;
import com.eventbooking.booking.dto.response.BookingResponse;
import com.eventbooking.booking.dto.response.EventDetailsResponse;
import com.eventbooking.booking.entity.Booking;
import com.eventbooking.booking.entity.BookingStatus;
import com.eventbooking.booking.exception.BookingAccessDeniedException;
import com.eventbooking.booking.exception.BookingAlreadyCancelledException;
import com.eventbooking.booking.exception.BookingNotFoundException;
import com.eventbooking.booking.repository.BookingRepository;
import com.eventbooking.booking.service.BookingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

	private final BookingRepository bookingRepository;
	private final EventServiceClient eventServiceClient;

	// Create Booking API
	@Override
	public BookingResponse createBooking(BookingRequest request, String authenticatedUserId,
			String authorizationHeader) {

		log.info("Creating booking for user: {}", authenticatedUserId);

		UUID userId = UUID.fromString(authenticatedUserId);

		// 1. Reserve seats through Event Service
		log.info("Requesting seat reservation for event: {}", request.getEventId());

		eventServiceClient.reserveSeats(request.getEventId().toString(), request.getNumberOfSeats(),
				authorizationHeader);

		// 2. Prepare booking entity
		Booking booking = new Booking();

		booking.setUserId(userId);
		booking.setEventId(request.getEventId());
		booking.setNumberOfSeats(request.getNumberOfSeats());
		booking.setBookingStatus(BookingStatus.CONFIRMED);

		LocalDateTime now = LocalDateTime.now();

		booking.setCreatedAt(now);
		booking.setUpdatedAt(now);

		// 3. Save booking
		Booking savedBooking = bookingRepository.save(booking);

		log.info("Booking created successfully. Booking ID: {}", savedBooking.getId());

		// 4. Return response
		return new BookingResponse(savedBooking.getId(), savedBooking.getUserId(), savedBooking.getEventId(),
				savedBooking.getNumberOfSeats(), savedBooking.getBookingStatus(), savedBooking.getCreatedAt(), null);
	}

	// Get Booking By ID API
	@Override
	public BookingResponse getBookingById(UUID bookingId, String authenticatedUserId, String authorizationHeader) {

		// 1. Find booking
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException("Booking not found"));

		// 2. Verify booking ownership
		if (!booking.getUserId().toString().equals(authenticatedUserId)) {
			throw new BookingAccessDeniedException("You are not allowed to view this booking");
		}

		// 3. Fetch event details
		EventDetailsResponse eventDetails = eventServiceClient.getEventById(booking.getEventId().toString(),
				authorizationHeader);

		// 4. Return combined response
		return new BookingResponse(booking.getId(), booking.getUserId(), booking.getEventId(),
				booking.getNumberOfSeats(), booking.getBookingStatus(), booking.getCreatedAt(), eventDetails);
	}

	// Get My Bookings API
	@Override
	public List<BookingResponse> getMyBookings(String authenticatedUserId, String authorizationHeader) {

		UUID userId = UUID.fromString(authenticatedUserId);

		// 1. Fetch bookings belonging to authenticated user
		List<Booking> bookings = bookingRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

		// 2. Fetch event details and prepare responses
		return bookings.stream().map(booking -> {

			EventDetailsResponse eventDetails = eventServiceClient.getEventById(booking.getEventId().toString(),
					authorizationHeader);

			return new BookingResponse(booking.getId(), booking.getUserId(), booking.getEventId(),
					booking.getNumberOfSeats(), booking.getBookingStatus(), booking.getCreatedAt(), eventDetails);
		}).toList();
	}

	// Cancel Booking API
	@Override
	public BookingCancellationResponse cancelBooking(UUID bookingId, String authenticatedUserId,
			String authorizationHeader) {

		log.info("Cancelling booking: {}", bookingId);

		// 1. Find booking
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException("Booking not found"));

		// 2. Verify booking ownership
		if (!booking.getUserId().toString().equals(authenticatedUserId)) {
			throw new BookingAccessDeniedException("You are not allowed to cancel this booking");
		}

		// 3. Check booking status
		if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
			throw new BookingAlreadyCancelledException("Booking is already cancelled");
		}

		// 4. Release seats through Event Service
		eventServiceClient.releaseSeats(booking.getEventId().toString(), booking.getNumberOfSeats(),
				authorizationHeader);

		// 5. Update booking status
		booking.setBookingStatus(BookingStatus.CANCELLED);
		booking.setUpdatedAt(LocalDateTime.now());

		// 6. Save updated booking
		Booking updatedBooking = bookingRepository.save(booking);

		log.info("Booking cancelled successfully: {}", updatedBooking.getId());

		// 7. Return response
		return new BookingCancellationResponse(updatedBooking.getId(), updatedBooking.getBookingStatus(),
				updatedBooking.getUpdatedAt(), "Booking cancelled successfully");
	}
}
