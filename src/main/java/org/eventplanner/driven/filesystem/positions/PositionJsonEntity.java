package org.eventplanner.driven.filesystem.positions;

import org.eventplanner.domain.positions.models.Position;
import org.eventplanner.domain.positions.models.PositionKey;
import org.eventplanner.domain.positions.models.Position;
import org.eventplanner.domain.positions.models.PositionKey;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record PositionJsonEntity(
    @NonNull String key,
    @NonNull String name,
    @NonNull String color,
    int prio

) implements Serializable {
    public Position toDomain() {
        return new Position(new PositionKey(key), name, color, prio);
    }
}
