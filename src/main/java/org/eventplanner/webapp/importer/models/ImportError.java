package org.eventplanner.webapp.importer.models;

import org.eventplanner.webapp.events.models.EventKey;
import org.springframework.lang.NonNull;

import java.time.Instant;

public record ImportError(
        @NonNull EventKey eventKey,
        @NonNull String eventName,
        @NonNull Instant start,
        @NonNull Instant end,
        @NonNull String message
) {
}
