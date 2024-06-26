package org.eventplanner.domain.importer.models;

import org.eventplanner.domain.events.models.EventKey;
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
