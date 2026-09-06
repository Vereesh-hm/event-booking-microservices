package com.eventbooking.event.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.eventbooking.event.dto.ApiResponse;
import com.eventbooking.event.dto.CreateEventRequest;
import com.eventbooking.event.dto.EventPageResponse;
import com.eventbooking.event.dto.EventResponse;
import com.eventbooking.event.dto.UpdateEventRequest;
import com.eventbooking.event.entity.Event;
import com.eventbooking.event.exception.DuplicateEventException;
import com.eventbooking.event.exception.EventAlreadyBookedException;
import com.eventbooking.event.exception.EventAlreadyExistsException;
import com.eventbooking.event.exception.EventNotFoundException;
import com.eventbooking.event.exception.InvalidEventQueryException;
import com.eventbooking.event.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

	@Mock
	private EventRepository eventRepository;

	@InjectMocks
	private EventServiceImpl eventService;

	// ---------------------------------------------------------
	// CREATE EVENT TESTS
	// ---------------------------------------------------------

	@Test
	void createEvent_shouldCreateEventSuccessfully() {

		// Arrange
		LocalDateTime eventDate = LocalDateTime.of(2026, 10, 10, 18, 30);

		CreateEventRequest request = new CreateEventRequest();

		request.setTitle("Java Conference");
		request.setDescription("Java and Spring Boot conference");
		request.setLocation("Bangalore");
		request.setEventDate(eventDate);
		request.setTotalSeats(200);

		when(eventRepository.findByTitleAndLocationAndEventDate(request.getTitle(), request.getLocation(),
				request.getEventDate())).thenReturn(Optional.empty());

		Event savedEvent = createEvent("Java Conference", "Java and Spring Boot conference", "Bangalore", eventDate,
				200, 200);

		when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

		// Act
		ApiResponse<EventResponse> response = eventService.createEvent(request);

		// Assert
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
		assertEquals("Event created successfully", response.getMessage());

		assertNotNull(response.getData());

		assertEquals("Java Conference", response.getData().getTitle());

		assertEquals("Bangalore", response.getData().getLocation());

		assertEquals(200, response.getData().getTotalSeats());

		assertEquals(200, response.getData().getAvailableSeats());

		verify(eventRepository).findByTitleAndLocationAndEventDate(request.getTitle(), request.getLocation(),
				request.getEventDate());

		verify(eventRepository).save(any(Event.class));
	}

	@Test
	void createEvent_shouldSetAvailableSeatsEqualToTotalSeats() {

		// Arrange
		CreateEventRequest request = new CreateEventRequest();

		request.setTitle("Music Concert");
		request.setDescription("Live music");
		request.setLocation("Mumbai");
		request.setEventDate(LocalDateTime.of(2026, 11, 15, 19, 0));
		request.setTotalSeats(500);

		when(eventRepository.findByTitleAndLocationAndEventDate(request.getTitle(), request.getLocation(),
				request.getEventDate())).thenReturn(Optional.empty());

		when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		eventService.createEvent(request);

		// Assert
		ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

		verify(eventRepository).save(eventCaptor.capture());

		Event savedEvent = eventCaptor.getValue();

		assertEquals(500, savedEvent.getTotalSeats());

		assertEquals(500, savedEvent.getAvailableSeats());

		assertNotNull(savedEvent.getId());
		assertNotNull(savedEvent.getCreatedAt());
		assertNotNull(savedEvent.getUpdatedAt());
	}

	@Test
	void createEvent_shouldThrowException_whenDuplicateEventExists() {

		// Arrange
		LocalDateTime eventDate = LocalDateTime.of(2026, 10, 10, 18, 30);

		CreateEventRequest request = new CreateEventRequest();

		request.setTitle("Java Conference");
		request.setDescription("Java conference");
		request.setLocation("Bangalore");
		request.setEventDate(eventDate);
		request.setTotalSeats(200);

		Event existingEvent = createEvent("Java Conference", "Java conference", "Bangalore", eventDate, 200, 200);

		when(eventRepository.findByTitleAndLocationAndEventDate(request.getTitle(), request.getLocation(),
				request.getEventDate())).thenReturn(Optional.of(existingEvent));

		// Act & Assert
		DuplicateEventException exception = assertThrows(DuplicateEventException.class,
				() -> eventService.createEvent(request));

		assertEquals("An event with the same title, location and date already exists", exception.getMessage());

		verify(eventRepository).findByTitleAndLocationAndEventDate(request.getTitle(), request.getLocation(),
				request.getEventDate());

		verify(eventRepository, never()).save(any(Event.class));
	}

	// ---------------------------------------------------------
	// GET EVENT BY ID TESTS
	// ---------------------------------------------------------

	@Test
	void getEventById_shouldReturnEventSuccessfully() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		Event event = createEvent("Tech Meetup", "Spring Boot meetup", "Bangalore",
				LocalDateTime.of(2026, 10, 20, 18, 0), 100, 80);

		event.setId(eventId);

		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

		// Act
		ApiResponse<EventResponse> response = eventService.getEventById(eventId);

		// Assert
		assertNotNull(response);
		assertTrue(response.isSuccess());

		assertEquals(HttpStatus.OK.value(), response.getStatusCode());

		assertEquals("Event details found successfully", response.getMessage());

		assertNotNull(response.getData());

		assertEquals(eventId, response.getData().getId());

		assertEquals("Tech Meetup", response.getData().getTitle());

		assertEquals(100, response.getData().getTotalSeats());

		assertEquals(80, response.getData().getAvailableSeats());

		verify(eventRepository).findById(eventId);
	}

	@Test
	void getEventById_shouldThrowException_whenEventDoesNotExist() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

		// Act & Assert
		EventNotFoundException exception = assertThrows(EventNotFoundException.class,
				() -> eventService.getEventById(eventId));

		assertEquals("Event details not found for the given id", exception.getMessage());

		verify(eventRepository).findById(eventId);
	}

	// ---------------------------------------------------------
	// GET ALL EVENTS TESTS
	// ---------------------------------------------------------

	@Test
	void getAllEvents_shouldReturnEventsSuccessfully() {

		// Arrange
		Event event1 = createEvent("Java Conference", "Conference", "Bangalore", LocalDateTime.of(2026, 10, 10, 18, 0),
				200, 200);

		Event event2 = createEvent("Spring Boot Meetup", "Meetup", "Mumbai", LocalDateTime.of(2026, 11, 10, 18, 0), 100,
				80);

		Page<Event> eventPage = new PageImpl<>(List.of(event1, event2));

		when(eventRepository.findAll(any(Pageable.class))).thenReturn(eventPage);

		// Act
		ApiResponse<EventPageResponse> response = eventService.getAllEvents(0, 10, "title", "asc");

		// Assert
		assertNotNull(response);
		assertTrue(response.isSuccess());

		assertEquals(HttpStatus.OK.value(), response.getStatusCode());

		assertEquals("Events fetched successfully", response.getMessage());

		assertNotNull(response.getData());

		assertEquals(2, response.getData().events().size());

		assertEquals(0, response.getData().page());

		assertEquals(2, response.getData().size());

		assertEquals(2, response.getData().totalElements());

		assertEquals(1, response.getData().totalPages());

		verify(eventRepository).findAll(any(Pageable.class));
	}

	@Test
	void getAllEvents_shouldReturnEmptyPage_whenNoEventsExist() {

		// Arrange
		Page<Event> emptyPage = new PageImpl<>(List.of());

		when(eventRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

		// Act
		ApiResponse<EventPageResponse> response = eventService.getAllEvents(0, 10, "title", "asc");

		// Assert
		assertNotNull(response);
		assertTrue(response.isSuccess());

		assertNotNull(response.getData());

		assertTrue(response.getData().events().isEmpty());

		assertEquals(0, response.getData().totalElements());

		assertEquals(1, response.getData().totalPages());

		verify(eventRepository).findAll(any(Pageable.class));
	}

	@Test
	void getAllEvents_shouldThrowException_whenPageIsNegative() {

		// Act & Assert
		InvalidEventQueryException exception = assertThrows(InvalidEventQueryException.class,
				() -> eventService.getAllEvents(-1, 10, "title", "asc"));

		assertEquals("Page number cannot be negative", exception.getMessage());

		verify(eventRepository, never()).findAll(any(Pageable.class));
	}

	@Test
	void getAllEvents_shouldThrowException_whenPageSizeIsInvalid() {

		// Act & Assert
		InvalidEventQueryException exception = assertThrows(InvalidEventQueryException.class,
				() -> eventService.getAllEvents(0, 101, "title", "asc"));

		assertEquals("Page size must be between 1 and 100", exception.getMessage());

		verify(eventRepository, never()).findAll(any(Pageable.class));
	}

	@Test
	void getAllEvents_shouldThrowException_whenSortFieldIsInvalid() {

		// Act & Assert
		InvalidEventQueryException exception = assertThrows(InvalidEventQueryException.class,
				() -> eventService.getAllEvents(0, 10, "invalidField", "asc"));

		assertEquals("Invalid sort field: invalidField", exception.getMessage());

		verify(eventRepository, never()).findAll(any(Pageable.class));
	}

	@Test
	void getAllEvents_shouldThrowException_whenSortDirectionIsInvalid() {

		// Act & Assert
		InvalidEventQueryException exception = assertThrows(InvalidEventQueryException.class,
				() -> eventService.getAllEvents(0, 10, "title", "invalid"));

		assertEquals("Invalid sort direction: invalid", exception.getMessage());

		verify(eventRepository, never()).findAll(any(Pageable.class));
	}

	// ---------------------------------------------------------
	// UPDATE EVENT TESTS
	// ---------------------------------------------------------

	@Test
	void updateEvent_shouldUpdateEventSuccessfully() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		Event existingEvent = createEvent("Old Title", "Old description", "Bangalore",
				LocalDateTime.of(2026, 10, 10, 18, 0), 200, 150);

		existingEvent.setId(eventId);

		UpdateEventRequest request = new UpdateEventRequest("New Title", "New description", "Mumbai",
				LocalDateTime.of(2026, 12, 10, 19, 0), 300);

		when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

		when(eventRepository.findByTitleAndLocationAndEventDate(request.title(), request.location(),
				request.eventDate())).thenReturn(Optional.empty());

		when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		ApiResponse<EventResponse> response = eventService.updateEvent(eventId, request);

		// Assert
		assertNotNull(response);
		assertTrue(response.isSuccess());

		assertEquals(HttpStatus.OK.value(), response.getStatusCode());

		assertEquals("Event updated successfully", response.getMessage());

		assertEquals("New Title", response.getData().getTitle());

		assertEquals(300, response.getData().getTotalSeats());

		// 50 seats were already booked.
		// New available seats = 300 - 50 = 250
		assertEquals(250, response.getData().getAvailableSeats());

		verify(eventRepository).save(existingEvent);
	}

	@Test
	void updateEvent_shouldIncreaseAvailableSeats_whenTotalSeatsIncrease() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		Event event = createEvent("Conference", "Conference", "Bangalore", LocalDateTime.of(2026, 10, 10, 18, 0), 200,
				150);

		event.setId(eventId);

		UpdateEventRequest request = new UpdateEventRequest("Conference", "Conference", "Bangalore",
				LocalDateTime.of(2026, 10, 10, 18, 0), 300);

		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

		when(eventRepository.findByTitleAndLocationAndEventDate(request.title(), request.location(),
				request.eventDate())).thenReturn(Optional.of(event));

		when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		ApiResponse<EventResponse> response = eventService.updateEvent(eventId, request);

		// Assert
		assertEquals(300, response.getData().getTotalSeats());

		assertEquals(250, response.getData().getAvailableSeats());
	}

	@Test
	void updateEvent_shouldAllowReducingSeats_whenAboveBookedSeats() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		Event event = createEvent("Conference", "Conference", "Bangalore", LocalDateTime.of(2026, 10, 10, 18, 0), 200,
				150);

		event.setId(eventId);

		// 50 seats are booked.
		// New total seats = 100.
		// This is valid because 100 > 50.
		UpdateEventRequest request = new UpdateEventRequest("Conference", "Conference", "Bangalore",
				LocalDateTime.of(2026, 10, 10, 18, 0), 100);

		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

		when(eventRepository.findByTitleAndLocationAndEventDate(request.title(), request.location(),
				request.eventDate())).thenReturn(Optional.of(event));

		when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		ApiResponse<EventResponse> response = eventService.updateEvent(eventId, request);

		// Assert
		assertEquals(100, response.getData().getTotalSeats());

		assertEquals(50, response.getData().getAvailableSeats());
	}

	@Test
	void updateEvent_shouldThrowException_whenTotalSeatsLessThanBookedSeats() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		Event event = createEvent("Conference", "Conference", "Bangalore", LocalDateTime.of(2026, 10, 10, 18, 0), 200,
				150);

		event.setId(eventId);

		// 50 seats already booked.
		// Trying to reduce total seats to 40.
		UpdateEventRequest request = new UpdateEventRequest("Conference", "Conference", "Bangalore",
				LocalDateTime.of(2026, 10, 10, 18, 0), 40);

		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

		// Act & Assert
		InvalidEventQueryException exception = assertThrows(InvalidEventQueryException.class,
				() -> eventService.updateEvent(eventId, request));

		assertEquals("Total seats cannot be less than already booked seats: 50", exception.getMessage());

		verify(eventRepository, never()).save(any(Event.class));
	}

	@Test
	void updateEvent_shouldThrowException_whenAnotherDuplicateEventExists() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		Event existingEvent = createEvent("Old Event", "Old description", "Bangalore",
				LocalDateTime.of(2026, 10, 10, 18, 0), 200, 200);

		existingEvent.setId(eventId);

		UUID anotherEventId = UUID.randomUUID();

		Event anotherEvent = createEvent("New Event", "New description", "Mumbai",
				LocalDateTime.of(2026, 12, 10, 19, 0), 300, 300);

		anotherEvent.setId(anotherEventId);

		UpdateEventRequest request = new UpdateEventRequest("New Event", "New description", "Mumbai",
				LocalDateTime.of(2026, 12, 10, 19, 0), 300);

		when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

		when(eventRepository.findByTitleAndLocationAndEventDate(request.title(), request.location(),
				request.eventDate())).thenReturn(Optional.of(anotherEvent));

		// Act & Assert
		EventAlreadyExistsException exception = assertThrows(EventAlreadyExistsException.class,
				() -> eventService.updateEvent(eventId, request));

		assertEquals("Another event already exists with the same title, location and event date",
				exception.getMessage());

		verify(eventRepository, never()).save(any(Event.class));
	}

	@Test
	void updateEvent_shouldAllowSameEventDetails() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		Event event = createEvent("Java Conference", "Java Conference", "Bangalore",
				LocalDateTime.of(2026, 10, 10, 18, 0), 200, 150);

		event.setId(eventId);

		UpdateEventRequest request = new UpdateEventRequest("Java Conference", "Java Conference", "Bangalore",
				LocalDateTime.of(2026, 10, 10, 18, 0), 200);

		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

		when(eventRepository.findByTitleAndLocationAndEventDate(request.title(), request.location(),
				request.eventDate())).thenReturn(Optional.of(event));

		when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		ApiResponse<EventResponse> response = eventService.updateEvent(eventId, request);

		// Assert
		assertNotNull(response);

		assertEquals("Java Conference", response.getData().getTitle());

		assertEquals(200, response.getData().getTotalSeats());

		assertEquals(150, response.getData().getAvailableSeats());

		verify(eventRepository).save(event);
	}

	@Test
	void updateEvent_shouldThrowException_whenEventDoesNotExist() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		UpdateEventRequest request = new UpdateEventRequest("Conference", "Conference", "Bangalore",
				LocalDateTime.of(2026, 10, 10, 18, 0), 200);

		when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

		// Act & Assert
		EventNotFoundException exception = assertThrows(EventNotFoundException.class,
				() -> eventService.updateEvent(eventId, request));

		assertEquals("Event details not found for the given id", exception.getMessage());

		verify(eventRepository, never()).save(any(Event.class));
	}

	// ---------------------------------------------------------
	// DELETE EVENT TESTS
	// ---------------------------------------------------------

	@Test
	void deleteEvent_shouldDeleteEventSuccessfully_whenNoSeatsBooked() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		Event event = createEvent("Java Conference", "Conference", "Bangalore", LocalDateTime.of(2026, 10, 10, 18, 0),
				200, 200);

		event.setId(eventId);

		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

		// Act
		ApiResponse<Void> response = eventService.deleteEvent(eventId);

		// Assert
		assertNotNull(response);
		assertTrue(response.isSuccess());

		assertEquals(HttpStatus.OK.value(), response.getStatusCode());

		assertEquals("Event deleted successfully", response.getMessage());

		verify(eventRepository).findById(eventId);

		verify(eventRepository).delete(event);
	}

	@Test
	void deleteEvent_shouldThrowException_whenEventDoesNotExist() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

		// Act & Assert
		EventNotFoundException exception = assertThrows(EventNotFoundException.class,
				() -> eventService.deleteEvent(eventId));

		assertEquals("Event details not found for the given id", exception.getMessage());

		verify(eventRepository, never()).delete(any(Event.class));
	}

	@Test
	void deleteEvent_shouldThrowException_whenSeatsAreAlreadyBooked() {

		// Arrange
		UUID eventId = UUID.randomUUID();

		// 200 total seats, 150 available = 50 booked.
		Event event = createEvent("Java Conference", "Conference", "Bangalore", LocalDateTime.of(2026, 10, 10, 18, 0),
				200, 150);

		event.setId(eventId);

		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

		// Act & Assert
		EventAlreadyBookedException exception = assertThrows(EventAlreadyBookedException.class,
				() -> eventService.deleteEvent(eventId));

		assertEquals("Event cannot be deleted because seats are already booked", exception.getMessage());

		verify(eventRepository, never()).delete(any(Event.class));
	}

	// ---------------------------------------------------------
	// HELPER METHOD
	// ---------------------------------------------------------

	private Event createEvent(String title, String description, String location, LocalDateTime eventDate,
			int totalSeats, int availableSeats) {

		Event event = new Event();

		event.setId(UUID.randomUUID());
		event.setTitle(title);
		event.setDescription(description);
		event.setLocation(location);
		event.setEventDate(eventDate);
		event.setTotalSeats(totalSeats);
		event.setAvailableSeats(availableSeats);
		event.setCreatedAt(LocalDateTime.of(2026, 9, 1, 10, 0));
		event.setUpdatedAt(LocalDateTime.of(2026, 9, 1, 10, 0));

		return event;
	}
}
