package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.events.models.EventLocation;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record EventLocationRepresentation(
        @NonNull String name,
        @NonNull String icon
) implements Serializable {
    public static @NonNull EventLocationRepresentation fromDomain(@NonNull EventLocation domain) {
        return new EventLocationRepresentation(domain.name(), domain.icon());
    }
}
