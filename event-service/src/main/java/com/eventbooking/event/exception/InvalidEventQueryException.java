package com.eventbooking.event.exception;

public class InvalidEventQueryException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidEventQueryException(String message) {
        super(message);
    }
}