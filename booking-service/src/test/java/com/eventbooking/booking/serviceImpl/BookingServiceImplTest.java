
package com.eventbooking.booking.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private EventServiceClient eventServiceClient;

	@InjectMocks
	private BookingServiceImpl bookingService;

	private UUID userId;
	private UUID anotherUserId;
	private UUID eventId;
	private UUID bookingId;

	private String authorizationHeader;

	private Booking booking;

	@BeforeEach
	void setUp() {

		userId = UUID.randomUUID();
		anotherUserId = UUID.randomUUID();
		eventId = UUID.randomUUID();
		bookingId = UUID.randomUUID();

		authorizationHeader = "Bearer test-token";

		booking = createBooking(bookingId, userId, eventId, 2, BookingStatus.CONFIRMED);
	}

	// -------------------------------------------------------
	// CREATE BOOKING TEST CASES
	// -------------------------------------------------------

	@Test
	void createBooking_shouldCreateBookingSuccessfully() {

		BookingRequest request = createBookingRequest(eventId, 2);

		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
			Booking savedBooking = invocation.getArgument(0);
			savedBooking.setId(bookingId);
			return savedBooking;
		});

		BookingResponse response = bookingService.createBooking(request, userId.toString(), authorizationHeader);

		assertNotNull(response);
		assertEquals(bookingId, response.bookingId());
		assertEquals(userId, response.userId());
		assertEquals(eventId, response.eventId());
		assertEquals(2, response.numberOfSeats());
		assertEquals(BookingStatus.CONFIRMED, response.bookingStatus());
		assertNotNull(response.createdAt());
		assertNull(response.event());

		verify(eventServiceClient, times(1)).reserveSeats(eventId.toString(), 2, authorizationHeader);

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		verify(bookingRepository, times(1)).save(bookingCaptor.capture());

		Booking savedBooking = bookingCaptor.getValue();

		assertEquals(userId, savedBooking.getUserId());
		assertEquals(eventId, savedBooking.getEventId());
		assertEquals(2, savedBooking.getNumberOfSeats());
		assertEquals(BookingStatus.CONFIRMED, savedBooking.getBookingStatus());
		assertNotNull(savedBooking.getCreatedAt());
		assertNotNull(savedBooking.getUpdatedAt());
	}

	@Test
	void createBooking_shouldThrowExceptionWhenSeatReservationFails() {

		BookingRequest request = createBookingRequest(eventId, 2);

		doThrow(new RuntimeException("Insufficient seats available")).when(eventServiceClient)
				.reserveSeats(eventId.toString(), 2, authorizationHeader);

		assertThrows(RuntimeException.class,
				() -> bookingService.createBooking(request, userId.toString(), authorizationHeader));

		verify(eventServiceClient, times(1)).reserveSeats(eventId.toString(), 2, authorizationHeader);

		verify(bookingRepository, never()).save(any(Booking.class));
	}

	@Test
	void createBooking_shouldPropagateExceptionWhenRepositorySaveFails() {

		BookingRequest request = createBookingRequest(eventId, 2);

		when(bookingRepository.save(any(Booking.class))).thenThrow(new RuntimeException("Database error"));

		assertThrows(RuntimeException.class,
				() -> bookingService.createBooking(request, userId.toString(), authorizationHeader));

		verify(eventServiceClient, times(1)).reserveSeats(eventId.toString(), 2, authorizationHeader);

		verify(bookingRepository, times(1)).save(any(Booking.class));
	}

	// -------------------------------------------------------
	// GET BOOKING BY ID TEST CASES
	// -------------------------------------------------------

	@Test
	void getBookingById_shouldReturnBookingSuccessfully() {

		EventDetailsResponse eventDetails = createEventDetails();

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

		when(eventServiceClient.getEventById(eventId.toString(), authorizationHeader)).thenReturn(eventDetails);

		BookingResponse response = bookingService.getBookingById(bookingId, userId.toString(), authorizationHeader);

		assertNotNull(response);
		assertEquals(bookingId, response.bookingId());
		assertEquals(userId, response.userId());
		assertEquals(eventId, response.eventId());
		assertEquals(2, response.numberOfSeats());
		assertEquals(BookingStatus.CONFIRMED, response.bookingStatus());
		assertNotNull(response.event());
		assertEquals("Java Backend Workshop", response.event().getTitle());

		verify(bookingRepository, times(1)).findById(bookingId);

		verify(eventServiceClient, times(1)).getEventById(eventId.toString(), authorizationHeader);
	}

	@Test
	void getBookingById_shouldThrowExceptionWhenBookingNotFound() {

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

		assertThrows(BookingNotFoundException.class,
				() -> bookingService.getBookingById(bookingId, userId.toString(), authorizationHeader));

		verify(bookingRepository, times(1)).findById(bookingId);

		verifyNoInteractions(eventServiceClient);
	}

	@Test
	void getBookingById_shouldThrowExceptionWhenUserDoesNotOwnBooking() {

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

		assertThrows(BookingAccessDeniedException.class,
				() -> bookingService.getBookingById(bookingId, anotherUserId.toString(), authorizationHeader));

		verify(bookingRepository, times(1)).findById(bookingId);

		verifyNoInteractions(eventServiceClient);
	}

	@Test
	void getBookingById_shouldPropagateExceptionWhenEventServiceFails() {

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

		when(eventServiceClient.getEventById(eventId.toString(), authorizationHeader))
				.thenThrow(new RuntimeException("Event Service unavailable"));

		assertThrows(RuntimeException.class,
				() -> bookingService.getBookingById(bookingId, userId.toString(), authorizationHeader));

		verify(eventServiceClient, times(1)).getEventById(eventId.toString(), authorizationHeader);
	}

	// -------------------------------------------------------
	// GET MY BOOKINGS TEST CASES
	// -------------------------------------------------------

	@Test
	void getMyBookings_shouldReturnAllUserBookings() {

		UUID secondBookingId = UUID.randomUUID();
		UUID secondEventId = UUID.randomUUID();

		Booking secondBooking = createBooking(secondBookingId, userId, secondEventId, 3, BookingStatus.CONFIRMED);

		EventDetailsResponse firstEvent = createEventDetails();
		EventDetailsResponse secondEvent = createEventDetails();
		secondEvent.setTitle("Spring Boot Workshop");

		when(bookingRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(booking, secondBooking));

		when(eventServiceClient.getEventById(eventId.toString(), authorizationHeader)).thenReturn(firstEvent);

		when(eventServiceClient.getEventById(secondEventId.toString(), authorizationHeader)).thenReturn(secondEvent);

		List<BookingResponse> responses = bookingService.getMyBookings(userId.toString(), authorizationHeader);

		assertNotNull(responses);
		assertEquals(2, responses.size());

		assertEquals(bookingId, responses.get(0).bookingId());
		assertEquals(eventId, responses.get(0).eventId());
		assertEquals("Java Backend Workshop", responses.get(0).event().getTitle());

		assertEquals(secondBookingId, responses.get(1).bookingId());
		assertEquals(secondEventId, responses.get(1).eventId());
		assertEquals("Spring Boot Workshop", responses.get(1).event().getTitle());

		verify(bookingRepository, times(1)).findAllByUserIdOrderByCreatedAtDesc(userId);

		verify(eventServiceClient, times(2)).getEventById(anyString(), eq(authorizationHeader));
	}

	@Test
	void getMyBookings_shouldReturnEmptyListWhenUserHasNoBookings() {

		when(bookingRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

		List<BookingResponse> responses = bookingService.getMyBookings(userId.toString(), authorizationHeader);

		assertNotNull(responses);
		assertTrue(responses.isEmpty());

		verify(bookingRepository, times(1)).findAllByUserIdOrderByCreatedAtDesc(userId);

		verifyNoInteractions(eventServiceClient);
	}

	@Test
	void getMyBookings_shouldPropagateExceptionWhenEventServiceFails() {

		when(bookingRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(booking));

		when(eventServiceClient.getEventById(eventId.toString(), authorizationHeader))
				.thenThrow(new RuntimeException("Event Service unavailable"));

		assertThrows(RuntimeException.class,
				() -> bookingService.getMyBookings(userId.toString(), authorizationHeader));

		verify(eventServiceClient, times(1)).getEventById(eventId.toString(), authorizationHeader);
	}

	// -------------------------------------------------------
	// CANCEL BOOKING TEST CASES
	// -------------------------------------------------------

	@Test
	void cancelBooking_shouldCancelBookingAndRestoreSeatsSuccessfully() {

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BookingCancellationResponse response = bookingService.cancelBooking(bookingId, userId.toString(),
				authorizationHeader);

		assertNotNull(response);
		assertEquals(bookingId, response.bookingId());
		assertEquals(BookingStatus.CANCELLED, response.bookingStatus());
		assertNotNull(response.updatedAt());
		assertEquals("Booking cancelled successfully", response.message());

		assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());

		verify(eventServiceClient, times(1)).releaseSeats(eventId.toString(), 2, authorizationHeader);

		verify(bookingRepository, times(1)).save(booking);
	}

	@Test
	void cancelBooking_shouldThrowExceptionWhenBookingNotFound() {

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

		assertThrows(BookingNotFoundException.class,
				() -> bookingService.cancelBooking(bookingId, userId.toString(), authorizationHeader));

		verify(eventServiceClient, never()).releaseSeats(anyString(), anyInt(), anyString());

		verify(bookingRepository, never()).save(any(Booking.class));
	}

	@Test
	void cancelBooking_shouldThrowExceptionWhenUserDoesNotOwnBooking() {

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

		assertThrows(BookingAccessDeniedException.class,
				() -> bookingService.cancelBooking(bookingId, anotherUserId.toString(), authorizationHeader));

		verify(eventServiceClient, never()).releaseSeats(anyString(), anyInt(), anyString());

		verify(bookingRepository, never()).save(any(Booking.class));
	}

	@Test
	void cancelBooking_shouldThrowExceptionWhenBookingAlreadyCancelled() {

		booking.setBookingStatus(BookingStatus.CANCELLED);

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

		assertThrows(BookingAlreadyCancelledException.class,
				() -> bookingService.cancelBooking(bookingId, userId.toString(), authorizationHeader));

		// Seats must not be restored again.
		verify(eventServiceClient, never()).releaseSeats(anyString(), anyInt(), anyString());

		// Already cancelled booking must not be saved again.
		verify(bookingRepository, never()).save(any(Booking.class));
	}

	@Test
	void cancelBooking_shouldNotUpdateBookingWhenSeatRestorationFails() {

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

		doThrow(new RuntimeException("Event Service unavailable")).when(eventServiceClient)
				.releaseSeats(eventId.toString(), 2, authorizationHeader);

		assertThrows(RuntimeException.class,
				() -> bookingService.cancelBooking(bookingId, userId.toString(), authorizationHeader));

		assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());

		verify(eventServiceClient, times(1)).releaseSeats(eventId.toString(), 2, authorizationHeader);

		verify(bookingRepository, never()).save(any(Booking.class));
	}

	// -------------------------------------------------------
	// TEST DATA HELPER METHODS
	// -------------------------------------------------------

	private Booking createBooking(UUID bookingId, UUID userId, UUID eventId, Integer numberOfSeats,
			BookingStatus status) {

		Booking booking = new Booking();

		booking.setId(bookingId);
		booking.setUserId(userId);
		booking.setEventId(eventId);
		booking.setNumberOfSeats(numberOfSeats);
		booking.setBookingStatus(status);
		booking.setCreatedAt(LocalDateTime.now());
		booking.setUpdatedAt(LocalDateTime.now());

		return booking;
	}

	private BookingRequest createBookingRequest(UUID eventId, Integer numberOfSeats) {

		BookingRequest request = new BookingRequest();

		request.setEventId(eventId);
		request.setNumberOfSeats(numberOfSeats);

		return request;
	}

	private EventDetailsResponse createEventDetails() {

		EventDetailsResponse event = new EventDetailsResponse();

		event.setTitle("Java Backend Workshop");
		event.setDescription("A practical Java backend workshop");
		event.setLocation("Bengaluru");
		event.setEventDate(LocalDateTime.now().plusDays(7));

		return event;
	}
}
