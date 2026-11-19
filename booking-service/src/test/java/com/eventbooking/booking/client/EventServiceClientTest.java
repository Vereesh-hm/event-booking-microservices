
package com.eventbooking.booking.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.eventbooking.booking.dto.response.EventDetailsResponse;

import tools.jackson.databind.json.JsonMapper;

class EventServiceClientTest {

	private EventServiceClient eventServiceClient;

	private HttpClient httpClient;

	private JsonMapper jsonMapper;

	private static final String BASE_URL = "http://localhost:8082";

	private static final String EVENT_ID = "550e8400-e29b-41d4-a716-446655440000";

	private static final String AUTHORIZATION = "Bearer test-token";

	@BeforeEach
	void setUp() throws Exception {

		jsonMapper = JsonMapper.builder().build();

		eventServiceClient = new EventServiceClient(jsonMapper);

		httpClient = mock(HttpClient.class);

		setField(eventServiceClient, "httpClient", httpClient);

		setField(eventServiceClient, "eventServiceBaseUrl", BASE_URL);
	}

	@AfterEach
	void clearInterruptFlag() {
		Thread.interrupted();
	}

	// --------------------------------------------------
	// RESERVE SEATS TEST CASES
	// --------------------------------------------------

	@Test
	void reserveSeats_shouldReserveSeatsSuccessfully() throws Exception {

		mockResponse(200, "");

		assertDoesNotThrow(() -> eventServiceClient.reserveSeats(EVENT_ID, 2, AUTHORIZATION));

		HttpRequest request = captureRequest();

		assertEquals(BASE_URL + "/api/events/" + EVENT_ID + "/reserve-seats", request.uri().toString());

		assertEquals("POST", request.method());

		assertEquals(AUTHORIZATION, request.headers().firstValue("Authorization").orElseThrow());

		assertEquals("application/json", request.headers().firstValue("Content-Type").orElseThrow());
	}

	@ParameterizedTest
	@CsvSource({ "400, 400", "401, 401", "403, 403", "404, 404", "409, 409", "429, 429", "500, 502" })
	void reserveSeats_shouldHandleErrorResponses(int responseStatus, int expectedStatus) throws Exception {

		mockResponse(responseStatus, "");

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.reserveSeats(EVENT_ID, 2, AUTHORIZATION));

		assertEquals(expectedStatus, exception.getStatusCode().value());
	}

	@Test
	void reserveSeats_shouldHandleTimeout() throws Exception {

		mockSendException(new HttpTimeoutException("Request timed out"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.reserveSeats(EVENT_ID, 2, AUTHORIZATION));

		assertEquals(HttpStatus.GATEWAY_TIMEOUT, exception.getStatusCode());
	}

	@Test
	void reserveSeats_shouldHandleIOException() throws Exception {

		mockSendException(new IOException("Connection refused"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.reserveSeats(EVENT_ID, 2, AUTHORIZATION));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
	}

	@Test
	void reserveSeats_shouldHandleInterruptedException() throws Exception {

		mockSendException(new InterruptedException("Request interrupted"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.reserveSeats(EVENT_ID, 2, AUTHORIZATION));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());

		assertTrue(Thread.currentThread().isInterrupted());
	}

	// --------------------------------------------------
	// GET EVENT BY ID TEST CASES
	// --------------------------------------------------

	@Test
	void getEventById_shouldReturnEventDetailsSuccessfully() throws Exception {

		String responseBody = """
				{
				    "success": true,
				    "statusCode": 200,
				    "message": "Event fetched successfully",
				    "data": {
				        "title": "Java Conference",
				        "description": "Java backend conference",
				        "location": "Bengaluru"
				    }
				}
				""";

		mockResponse(200, responseBody);

		EventDetailsResponse response = eventServiceClient.getEventById(EVENT_ID, AUTHORIZATION);

		assertNotNull(response);

		assertEquals("Java Conference", response.getTitle());

		assertEquals("Java backend conference", response.getDescription());

		assertEquals("Bengaluru", response.getLocation());

		HttpRequest request = captureRequest();

		assertEquals(BASE_URL + "/api/events/" + EVENT_ID, request.uri().toString());

		assertEquals("GET", request.method());

		assertEquals(AUTHORIZATION, request.headers().firstValue("Authorization").orElseThrow());
	}

	@ParameterizedTest
	@CsvSource({ "401, 401", "403, 403", "404, 404", "429, 429", "500, 502" })
	void getEventById_shouldHandleErrorResponses(int responseStatus, int expectedStatus) throws Exception {

		mockResponse(responseStatus, "");

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.getEventById(EVENT_ID, AUTHORIZATION));

		assertEquals(expectedStatus, exception.getStatusCode().value());
	}

	@Test
	void getEventById_shouldHandleNullEventData() throws Exception {

		mockResponse(200, """
				{
				    "success": true,
				    "statusCode": 200,
				    "message": "Success",
				    "data": null
				}
				""");

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.getEventById(EVENT_ID, AUTHORIZATION));

		assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
	}

	@Test
	void getEventById_shouldHandleMissingEventData() throws Exception {

		mockResponse(200, """
				{
				    "success": true,
				    "statusCode": 200,
				    "message": "Success"
				}
				""");

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.getEventById(EVENT_ID, AUTHORIZATION));

		assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
	}

	@Test
	void getEventById_shouldHandleInvalidJsonResponse() throws Exception {

		mockResponse(200, "invalid-json-response");

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.getEventById(EVENT_ID, AUTHORIZATION));

		assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
	}

	@Test
	void getEventById_shouldHandleTimeout() throws Exception {

		mockSendException(new HttpTimeoutException("Request timed out"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.getEventById(EVENT_ID, AUTHORIZATION));

		assertEquals(HttpStatus.GATEWAY_TIMEOUT, exception.getStatusCode());
	}

	@Test
	void getEventById_shouldHandleIOException() throws Exception {

		mockSendException(new IOException("Connection refused"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.getEventById(EVENT_ID, AUTHORIZATION));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
	}

	@Test
	void getEventById_shouldHandleInterruptedException() throws Exception {

		mockSendException(new InterruptedException("Request interrupted"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.getEventById(EVENT_ID, AUTHORIZATION));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());

		assertTrue(Thread.currentThread().isInterrupted());
	}

	// --------------------------------------------------
	// RELEASE SEATS TEST CASES
	// --------------------------------------------------

	@Test
	void releaseSeats_shouldReleaseSeatsSuccessfully() throws Exception {

		mockResponse(200, "");

		assertDoesNotThrow(() -> eventServiceClient.releaseSeats(EVENT_ID, 2, AUTHORIZATION));

		HttpRequest request = captureRequest();

		assertEquals(BASE_URL + "/api/events/" + EVENT_ID + "/release-seats", request.uri().toString());

		assertEquals("POST", request.method());

		assertEquals(AUTHORIZATION, request.headers().firstValue("Authorization").orElseThrow());

		assertEquals("application/json", request.headers().firstValue("Content-Type").orElseThrow());
	}

	@ParameterizedTest
	@CsvSource({ "400, 400", "401, 401", "403, 403", "404, 404", "409, 409", "429, 429", "500, 502" })
	void releaseSeats_shouldHandleErrorResponses(int responseStatus, int expectedStatus) throws Exception {

		mockResponse(responseStatus, "");

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.releaseSeats(EVENT_ID, 2, AUTHORIZATION));

		assertEquals(expectedStatus, exception.getStatusCode().value());
	}

	@Test
	void releaseSeats_shouldHandleTimeout() throws Exception {

		mockSendException(new HttpTimeoutException("Request timed out"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.releaseSeats(EVENT_ID, 2, AUTHORIZATION));

		assertEquals(HttpStatus.GATEWAY_TIMEOUT, exception.getStatusCode());
	}

	@Test
	void releaseSeats_shouldHandleIOException() throws Exception {

		mockSendException(new IOException("Connection refused"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.releaseSeats(EVENT_ID, 2, AUTHORIZATION));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
	}

	@Test
	void releaseSeats_shouldHandleInterruptedException() throws Exception {

		mockSendException(new InterruptedException("Request interrupted"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> eventServiceClient.releaseSeats(EVENT_ID, 2, AUTHORIZATION));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());

		assertTrue(Thread.currentThread().isInterrupted());
	}

	// --------------------------------------------------
	// COMMON HELPER METHODS
	// --------------------------------------------------

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void mockResponse(int statusCode, String responseBody) throws Exception {

		HttpResponse<String> response = mock(HttpResponse.class);

		when(response.statusCode()).thenReturn(statusCode);

		when(response.body()).thenReturn(responseBody);

		doReturn(response).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
	}

	private void mockSendException(Exception exception) throws Exception {

		doThrow(exception).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private HttpRequest captureRequest() throws Exception {

		ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

		verify(httpClient).send(captor.capture(), any(HttpResponse.BodyHandler.class));

		return captor.getValue();
	}

	private void setField(Object target, String fieldName, Object value) throws Exception {

		Field field = target.getClass().getDeclaredField(fieldName);

		field.setAccessible(true);

		field.set(target, value);
	}
}
