package org.eventplanner.domain.users.models;

import org.eventplanner.domain.positions.models.PositionKey;
import org.eventplanner.domain.positions.models.PositionKey;
import org.springframework.lang.NonNull;

import java.util.List;

public record User(
    @NonNull UserKey key,
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull List<PositionKey> positions
) {
}
