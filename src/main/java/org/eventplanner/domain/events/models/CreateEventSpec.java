package org.eventplanner.domain.events.models;

import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.List;

public record CreateEventSpec(
    @Nullable String name,
    @Nullable String note,
    @Nullable String description,
    @Nullable ZonedDateTime start,
    @Nullable ZonedDateTime end,
    @Nullable List<Location> locations,
    @Nullable List<Slot> slots
) {
}