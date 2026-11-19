
package com.eventbooking.booking.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.eventbooking.booking.dto.response.EventDetailsResponse;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class EventServiceClient {

	private static final Logger log = LoggerFactory.getLogger(EventServiceClient.class);

	private final HttpClient httpClient;
	private final JsonMapper jsonMapper;

	@Value("${event-service.base-url}")
	private String eventServiceBaseUrl;

	public EventServiceClient(JsonMapper jsonMapper) {
		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		this.jsonMapper = jsonMapper;
	}

	// Reserve Seats
	public void reserveSeats(String eventId, Integer numberOfSeats, String authorizationHeader) {

		try {
			String requestBody = jsonMapper.writeValueAsString(Map.of("numberOfSeats", numberOfSeats));

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(eventServiceBaseUrl + "/api/events/" + eventId + "/reserve-seats"))
					.timeout(Duration.ofSeconds(5)).header("Content-Type", "application/json")
					.header("Authorization", authorizationHeader).POST(HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			log.info("Requesting seat reservation. Event ID: {}, Seats: {}", eventId, numberOfSeats);

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			int statusCode = response.statusCode();

			log.info("Seat reservation response. Event ID: {}, Status: {}", eventId, statusCode);

			switch (statusCode) {
			case 200 -> log.info("Seats reserved successfully for event: {}", eventId);

			case 400 -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid seat reservation request");

			case 401 -> throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"Authentication failed while communicating with Event Service");

			case 403 -> throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Not authorized to reserve seats for this event");

			case 404 -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");

			case 409 -> throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient seats available");

			case 429 -> throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Event Service is receiving too many requests");

			default -> {
				log.error("Unexpected reservation response. Event ID: {}, Status: {}", eventId, statusCode);

				throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
						"Unexpected response received from Event Service");
			}
			}

		} catch (HttpTimeoutException e) {
			log.error("Seat reservation timed out. Event ID: {}", eventId, e);

			throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Event Service request timed out");

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Seat reservation interrupted. Event ID: {}", eventId, e);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Event Service request was interrupted");

		} catch (IOException e) {
			log.error("Communication with Event Service failed. Event ID: {}", eventId, e);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Unable to communicate with Event Service");
		}
	}

	// Get Event By ID
	public EventDetailsResponse getEventById(String eventId, String authorizationHeader) {

		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(eventServiceBaseUrl + "/api/events/" + eventId)).timeout(Duration.ofSeconds(5))
					.header("Authorization", authorizationHeader).GET().build();

			log.info("Fetching event details. Event ID: {}", eventId);

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			int statusCode = response.statusCode();

			log.info("Get event response. Event ID: {}, Status: {}", eventId, statusCode);

			if (statusCode == 404) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
			}

			if (statusCode == 401) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"Authentication failed while communicating with Event Service");
			}

			if (statusCode == 403) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to access event details");
			}

			if (statusCode == 429) {
				throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
						"Event Service is receiving too many requests");
			}

			if (statusCode != 200) {
				log.error("Unexpected get event response. Event ID: {}, Status: {}", eventId, statusCode);

				throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
						"Unable to fetch event details from Event Service");
			}

			JsonNode responseJson = jsonMapper.readTree(response.body());
			JsonNode eventData = responseJson.get("data");

			if (eventData == null || eventData.isNull()) {
				throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
						"Event details missing in Event Service response");
			}

			return jsonMapper.readValue(eventData.toString(), EventDetailsResponse.class);

		} catch (HttpTimeoutException e) {
			log.error("Get event request timed out. Event ID: {}", eventId, e);

			throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Event Service request timed out");

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Get event request interrupted. Event ID: {}", eventId, e);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Event Service request was interrupted");

		} catch (IOException e) {
			log.error("Communication with Event Service failed. Event ID: {}", eventId, e);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Unable to communicate with Event Service");

		} catch (ResponseStatusException e) {
			throw e;

		} catch (Exception e) {
			log.error("Invalid Event Service response. Event ID: {}", eventId, e);

			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid response received from Event Service");
		}
	}

	// Release Seats
	public void releaseSeats(String eventId, Integer numberOfSeats, String authorizationHeader) {

		try {
			String requestBody = jsonMapper.writeValueAsString(Map.of("numberOfSeats", numberOfSeats));

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(eventServiceBaseUrl + "/api/events/" + eventId + "/release-seats"))
					.timeout(Duration.ofSeconds(5)).header("Content-Type", "application/json")
					.header("Authorization", authorizationHeader).POST(HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			log.info("Requesting seat release. Event ID: {}, Seats: {}", eventId, numberOfSeats);

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			int statusCode = response.statusCode();

			log.info("Seat release response. Event ID: {}, Status: {}", eventId, statusCode);

			switch (statusCode) {
			case 200 -> log.info("Seats released successfully for event: {}", eventId);

			case 400 -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid seat release request");

			case 401 -> throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
					"Authentication failed while communicating with Event Service");

			case 403 -> throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Not authorized to release seats for this event");

			case 404 -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");

			case 409 -> throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to release seats");

			case 429 -> throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Event Service is receiving too many requests");

			default -> {
				log.error("Unexpected seat release response. Event ID: {}, Status: {}", eventId, statusCode);

				throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
						"Unexpected response received from Event Service");
			}
			}

		} catch (HttpTimeoutException e) {
			log.error("Seat release timed out. Event ID: {}", eventId, e);

			throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Event Service request timed out");

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Seat release interrupted. Event ID: {}", eventId, e);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Event Service request was interrupted");

		} catch (IOException e) {
			log.error("Communication with Event Service failed. Event ID: {}", eventId, e);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Unable to communicate with Event Service");
		}
	}
}
