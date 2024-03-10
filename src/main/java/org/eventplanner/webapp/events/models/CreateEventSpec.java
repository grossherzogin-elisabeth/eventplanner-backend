package org.eventplanner.webapp.events.models;

import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;

public record CreateEventSpec(
        @Nullable String name,
        @Nullable String note,
        @Nullable String description,
        @Nullable Instant start,
        @Nullable Instant end,
        @Nullable List<Location> locations,
        @Nullable List<Slot> slots
) {
}