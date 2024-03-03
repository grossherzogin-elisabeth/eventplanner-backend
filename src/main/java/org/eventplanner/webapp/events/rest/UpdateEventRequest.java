package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.events.models.UpdateEventSpec;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record UpdateEventRequest(
        @Nullable String name,
        @Nullable String state,
        @Nullable String note,
        @Nullable String description,
        @Nullable String start,
        @Nullable String end,
        @Nullable List<EventLocationRepresentation> locations
) implements Serializable {
    public UpdateEventSpec toDomain() {
        return new UpdateEventSpec(
                name,
                state,
                note,
                description,
                start != null ? Instant.parse(start) : null,
                end != null ? Instant.parse(end) : null,
                locations != null ? locations.stream().map(EventLocationRepresentation::toDomain).toList() : null
        );
    }
}
