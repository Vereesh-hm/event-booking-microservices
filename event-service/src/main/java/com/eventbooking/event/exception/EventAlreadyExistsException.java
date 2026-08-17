package com.eventbooking.event.exception;

public class EventAlreadyExistsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EventAlreadyExistsException(String message) {
		super(message);
	}
}
