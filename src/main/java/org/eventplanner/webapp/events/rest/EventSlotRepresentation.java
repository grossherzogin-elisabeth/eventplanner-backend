package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.events.models.EventSlot;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.List;

public record EventSlotRepresentation(
        String key,
        int order,
        boolean required,
        @NonNull List<String> positionKeys,
        @Nullable String name,
        @Nullable String assignedUserKey,
        @Nullable String assignedPositionKey
) implements Serializable {
    public static @NonNull EventSlotRepresentation fromDomain(@NonNull EventSlot domain) {
        return new EventSlotRepresentation(
                "",
                domain.order(),
                domain.required(),
                domain.positions().stream().map((PositionKey::value)).toList(),
                domain.name(),
                domain.assignedUser() != null ? domain.assignedUser().value() : null,
                domain.assignedPosition() != null ? domain.assignedPosition().value() : null
        );
    }

    public @NonNull EventSlot toDomain() {
        return new EventSlot(
                order,
                required,
                positionKeys().stream().map((PositionKey::new)).toList(),
                name,
                assignedUserKey != null ? new UserKey(assignedUserKey) : null,
                assignedPositionKey != null ? new PositionKey(assignedPositionKey) : null
        );
    }
}
