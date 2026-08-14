package com.eventbooking.event.serviceImpl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.eventbooking.event.dto.ApiResponse;
import com.eventbooking.event.dto.CreateEventRequest;
import com.eventbooking.event.dto.EventPageResponse;
import com.eventbooking.event.dto.EventResponse;
import com.eventbooking.event.entity.Event;
import com.eventbooking.event.exception.DuplicateEventException;
import com.eventbooking.event.exception.EventNotFoundException;
import com.eventbooking.event.exception.InvalidEventQueryException;
import com.eventbooking.event.repository.EventRepository;
import com.eventbooking.event.service.EventService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventServiceImpl implements EventService {

	private final EventRepository eventRepository;

	public EventServiceImpl(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	@Transactional
	public ApiResponse<EventResponse> createEvent(CreateEventRequest createEventRequest) {

		log.info("Event creation request received. title={}, location={}, eventDate={}", createEventRequest.getTitle(),
				createEventRequest.getLocation(), createEventRequest.getEventDate());

		if (eventRepository.findByTitleAndLocationAndEventDate(createEventRequest.getTitle(),
				createEventRequest.getLocation(), createEventRequest.getEventDate()).isPresent()) {

			log.warn("Duplicate event creation attempt. title={}, location={}, eventDate={}",
					createEventRequest.getTitle(), createEventRequest.getLocation(), createEventRequest.getEventDate());

			throw new DuplicateEventException("An event with the same title, location and date already exists");
		}

		Event event = new Event();

		event.setId(UUID.randomUUID());
		event.setDescription(createEventRequest.getDescription());
		event.setLocation(createEventRequest.getLocation());
		event.setTitle(createEventRequest.getTitle());
		event.setEventDate(createEventRequest.getEventDate());
		event.setTotalSeats(createEventRequest.getTotalSeats());

		// Initially all seats are available
		event.setAvailableSeats(createEventRequest.getTotalSeats());

		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		event.setCreatedAt(now);
		event.setUpdatedAt(now);

		Event savedEvent = eventRepository.save(event);

		log.info("Event created successfully. eventId={}, title={}", savedEvent.getId(), savedEvent.getTitle());

		EventResponse eventResponse = new EventResponse(savedEvent.getId(), savedEvent.getTitle(),
				savedEvent.getDescription(), savedEvent.getLocation(), savedEvent.getEventDate(),
				savedEvent.getTotalSeats(), savedEvent.getAvailableSeats(), savedEvent.getCreatedAt(),
				savedEvent.getUpdatedAt());

		return new ApiResponse<>(true, HttpStatus.CREATED.value(), "Event created successfully", eventResponse);
	}

	@Override
	public ApiResponse<EventResponse> getEventById(UUID id) {

		Event event = eventRepository.findById(id).orElseThrow(() -> {
			log.warn("Event details not found for id: {}", id);

			return new EventNotFoundException("Event details not found for the given id");
		});

		EventResponse eventResponse = mapToEventResponse(event);

		log.info("Event details fetched successfully for id: {}", id);

		return new ApiResponse<>(true, HttpStatus.OK.value(), "Event details found successfully", eventResponse);
	}

	private EventResponse mapToEventResponse(Event event) {

		return new EventResponse(event.getId(), event.getTitle(), event.getDescription(), event.getLocation(),
				event.getEventDate(), event.getTotalSeats(), event.getAvailableSeats(), event.getCreatedAt(),
				event.getUpdatedAt());
	}

	private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("title", "location", "eventDate", "totalSeats",
			"availableSeats", "createdAt", "updatedAt");

	private static final Set<String> ALLOWED_SORT_DIRECTION_FIELDS = Set.of("asc", "desc");

	@Override
	public ApiResponse<EventPageResponse> getAllEvents(int page, int size, String sortBy, String sortDirection) {

		if (page < 0) {
			throw new InvalidEventQueryException("Page number cannot be negative");
		}

		if (size <= 0 || size > 100) {
			throw new InvalidEventQueryException("Page size must be between 1 and 100");
		}

		if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
			throw new InvalidEventQueryException("Invalid sort field: " + sortBy);
		}

		if (!ALLOWED_SORT_DIRECTION_FIELDS.contains(sortDirection)) {
			throw new InvalidEventQueryException("Invalid sort direction: " + sortDirection);
		}

		Sort.Direction direction = Sort.Direction.fromString(sortDirection);

		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

		Page<Event> events = eventRepository.findAll(pageable);

		List<EventResponse> eventResponses = events.getContent().stream().map(this::mapToEventResponse).toList();

		EventPageResponse eventPageResponse = new EventPageResponse(eventResponses, events.getNumber(),
				events.getSize(), events.getTotalElements(), events.getTotalPages());

		log.info("Events fetched successfully. page: {}, size: {}, totalElements: {}", page, size,
				events.getTotalElements());

		return new ApiResponse<>(true, HttpStatus.OK.value(), "Events fetched successfully", eventPageResponse);
	}
}
