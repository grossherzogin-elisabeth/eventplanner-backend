package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.events.models.Slot;
import org.eventplanner.webapp.events.models.SlotKey;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.List;

public record SlotRepresentation(
        @NonNull String key,
        int order,
        boolean required,
        @NonNull List<String> positionKeys,
        @Nullable String name
) implements Serializable {

    public static @NonNull SlotRepresentation fromDomain(@NonNull Slot domain) {
        return new SlotRepresentation(
                domain.key().value(),
                domain.order(),
                domain.required(),
                domain.positions().stream().map((PositionKey::value)).toList(),
                domain.name());
    }

    public @NonNull Slot toDomain() {
        return new Slot(
                new SlotKey(key),
                order,
                required,
                positionKeys().stream().map((PositionKey::new)).toList(),
                name);
    }
}
