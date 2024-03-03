package org.eventplanner.webapp.positions.rest;

import org.eventplanner.webapp.positions.models.Position;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record PositionRepresentation(
        @NonNull String key,
        @NonNull String name,
        @NonNull String color
) implements Serializable {

    public static PositionRepresentation fromDomain(@NonNull Position position) {
        return new PositionRepresentation(
                position.key().value(),
                position.name(),
                position.color()
        );
    }
}
