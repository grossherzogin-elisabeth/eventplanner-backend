package org.eventplanner.webapp.events.filesystem;

import org.eventplanner.webapp.events.models.EventSlot;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;

public record EventSlotJsonEntity(
        int order,
        boolean required,
        @Nullable List<String> positions,
        @Nullable String name,
        @Nullable String assignedUser,
        @Nullable String assignedPersonName,
        @Nullable String assignedPosition
) {

    public static @NonNull EventSlotJsonEntity fromDomain(@NonNull EventSlot domain) {
        return new EventSlotJsonEntity(
                domain.order(),
                domain.required(),
                domain.positions().stream().map(PositionKey::value).toList(),
                domain.name(),
                domain.assignedUser() != null
                        ? domain.assignedUser().value()
                        : null,
                domain.assignedPersonName(),
                domain.assignedPosition() != null
                        ? domain.assignedPosition().value()
                        : null);
    }

    public EventSlot toDomain() {
        return new EventSlot(
                order,
                required,
                positions != null
                        ? positions.stream().map(PositionKey::new).toList()
                        : Collections.emptyList(),
                name,
                assignedUser != null
                        ? new UserKey(assignedUser)
                        : null,
                assignedPersonName,
                assignedPosition != null
                        ? new PositionKey(assignedPosition)
                        : null);
    }
}
