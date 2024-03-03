package org.eventplanner.webapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MissingPermissionException extends RuntimeException {
    public MissingPermissionException() {
        super();
    }

    public MissingPermissionException(String message) {
        super(message);
    }
}
