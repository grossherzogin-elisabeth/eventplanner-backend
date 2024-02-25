package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.events.models.Event;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public record EventRepresentation(
        @NonNull String key,
        @NonNull String name,
        @NonNull String note,
        @NonNull String description,
        @NonNull String start,
        @NonNull String end,
        @NonNull Map<String, String> waitingList
) implements Serializable {

    public static EventRepresentation fromDomain(@NonNull Event event) {
        return new EventRepresentation(
                event.key(),
                event.name(),
                event.note(),
                event.description(),
                event.start().toString(),
                event.end().toString(),
                event.waitingList()
        );
    }
}
