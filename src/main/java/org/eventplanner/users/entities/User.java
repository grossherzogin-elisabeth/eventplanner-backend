package org.eventplanner.users.entities;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;

import java.util.List;

public record User(
    @NonNull UserKey key,
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull List<PositionKey> positions
) {
}
