package org.eventplanner.webapp.events.models;

import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;

public record UpdateEventSpec(
        @Nullable String name,
        @Nullable String state,
        @Nullable String note,
        @Nullable String description,
        @Nullable Instant start,
        @Nullable Instant end,
        @Nullable List<EventLocation> locations
) {
}