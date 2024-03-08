package org.eventplanner.webapp.users.models;

import org.eventplanner.webapp.positions.models.PositionKey;
import org.springframework.lang.NonNull;

import java.util.List;

public record User(
        @NonNull UserKey key,
        @NonNull String firstName,
        @NonNull String lastName,
        @NonNull List<PositionKey> positions
) {
}
