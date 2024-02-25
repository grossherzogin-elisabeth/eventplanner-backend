package org.eventplanner.webapp.positions.mock;

import org.eventplanner.webapp.positions.models.PositionKey;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public record PositionJsonEntity(
        @NonNull String key,
        @NonNull String name,
        @NonNull String color,
        @NonNull int prio,
        @NonNull List<String> substitutes
) implements Serializable {
}
