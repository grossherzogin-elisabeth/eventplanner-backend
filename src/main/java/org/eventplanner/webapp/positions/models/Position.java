package org.eventplanner.webapp.positions.models;

import org.springframework.lang.NonNull;

import java.util.List;

public record Position(
        @NonNull PositionKey key,
        @NonNull String name,
        @NonNull String color
) {
}
