package org.eventplanner.webapp.users.rest;

import org.springframework.lang.NonNull;

import java.io.Serializable;

public record AccountRepresentation(
        @NonNull String key,
        @NonNull String email
) implements Serializable {
}
