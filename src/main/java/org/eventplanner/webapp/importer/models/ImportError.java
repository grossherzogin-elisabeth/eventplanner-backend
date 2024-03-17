package org.eventplanner.webapp.importer.models;

import org.eventplanner.webapp.events.models.EventKey;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;

public record ImportError(
        @NonNull EventKey eventKey,
        @NonNull String eventName,
        @NonNull ZonedDateTime start,
        @NonNull ZonedDateTime end,
        @NonNull String message
) {
}
