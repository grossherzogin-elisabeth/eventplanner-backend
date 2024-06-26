package org.eventplanner.domain.positions.models;

import org.springframework.lang.NonNull;

public record Position(
    @NonNull PositionKey key,
    @NonNull String name,
    @NonNull String color,
    int priority
) {
}
