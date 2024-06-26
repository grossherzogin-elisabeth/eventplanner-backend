package org.eventplanner.driven.filesystem.events.models;

import org.eventplanner.domain.events.models.Slot;
import org.eventplanner.domain.positions.models.PositionKey;
import org.eventplanner.domain.events.models.Slot;
import org.eventplanner.domain.events.models.SlotKey;
import org.eventplanner.domain.positions.models.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;

import static org.eventplanner.domain.utils.ObjectUtils.mapNullable;

public record SlotJsonEntity(
    @NonNull String key,
    int order,
    boolean required,
    @Nullable List<String> positions,
    @Nullable String name
) {

    public static @NonNull SlotJsonEntity fromDomain(@NonNull Slot domain) {
        return new SlotJsonEntity(
            domain.key().value(),
            domain.order(),
            domain.required(),
            domain.positions().stream().map(PositionKey::value).toList(),
            domain.name());
    }

    public @NonNull Slot toDomain() {
        return new Slot(
            new SlotKey(key),
            order,
            required,
            mapNullable(positions, PositionKey::new, Collections.emptyList()),
            name);
    }
}
