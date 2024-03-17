package org.eventplanner.webapp.events.models;

import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;
import java.util.List;

public record Event(
        @NonNull EventKey key,
        @NonNull String name,
        @NonNull EventState state,
        @NonNull String note,
        @NonNull String description,
        @NonNull ZonedDateTime start,
        @NonNull ZonedDateTime end,
        @NonNull List<Location> locations,
        @NonNull List<Slot> slots,
        @NonNull List<Registration> registrations
) {
}