package org.eventplanner.webapp.positions.json;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public record PositionJsonEntity(
        @NonNull String key,
        @NonNull String name,
        @NonNull String color
) implements Serializable {
}
