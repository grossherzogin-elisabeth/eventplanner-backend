package org.eventplanner.webapp.events.models;

import org.springframework.lang.NonNull;

public record EventPosition(
        @NonNull String positionKey,
        @NonNull boolean required,
        @NonNull String userKey
) {
}
