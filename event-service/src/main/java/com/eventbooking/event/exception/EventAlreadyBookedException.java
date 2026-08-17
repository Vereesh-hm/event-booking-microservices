package com.eventbooking.event.exception;

public class EventAlreadyBookedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EventAlreadyBookedException(String message) {
		super(message);
	}

}
