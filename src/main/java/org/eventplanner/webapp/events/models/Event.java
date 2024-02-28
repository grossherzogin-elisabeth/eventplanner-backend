package org.eventplanner.webapp.events.models;

import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Map;

public record Event(
        @NonNull String key,
        @NonNull String name,
        @NonNull String templateKey,
        @NonNull String state,
        @NonNull String note,
        @NonNull String description,
        @NonNull Instant start,
        @NonNull Instant end,
        @NonNull Map<UserKey, PositionKey> waitingList
) {
}