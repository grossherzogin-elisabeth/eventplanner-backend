package org.eventplanner.webapp.positions.rest;

import org.eventplanner.webapp.positions.models.Position;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public record PositionRepresentation(
        @NonNull String key,
        @NonNull String name,
        @NonNull String color,
        @NonNull int prio,
        @NonNull List<String> substitutes
) implements Serializable {

    public static PositionRepresentation fromDomain(@NonNull Position position) {
        return new PositionRepresentation(
                position.key().value(),
                position.name(),
                position.color(),
                position.prio(),
                position.substitutes().stream().map(PositionKey::value).toList()
        );
    }
}
