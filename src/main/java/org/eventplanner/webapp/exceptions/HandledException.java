package org.eventplanner.webapp.exceptions;

public abstract class HandledException extends RuntimeException {
    public HandledException() {
        super();
    }

    public HandledException(String message) {
        super(message);
    }
}
