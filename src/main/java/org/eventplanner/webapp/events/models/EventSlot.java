package org.eventplanner.webapp.events.models;

import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public record EventSlot(
        int order,
        boolean required,
        @NonNull List<PositionKey> positions,
        @Nullable String name,
        @Nullable UserKey assignedUser,
        @Nullable PositionKey assignedPosition
) {

    public static EventSlot of(PositionKey... positions) {
        return new EventSlot(0, false, List.of(positions), null, null, null);
    }

    public EventSlot withRequired() {
        return new EventSlot(order, true, positions, name, assignedUser, assignedPosition);
    }

    public EventSlot withPositions(PositionKey... positions) {
        return new EventSlot(order, required, List.of(positions), name, assignedUser, assignedPosition);
    }

    public EventSlot withOrder(int order) {
        return new EventSlot(order, required, positions, name, assignedUser, assignedPosition);
    }

    public EventSlot withName(String name) {
        return new EventSlot(order, required, positions, name, assignedUser, assignedPosition);
    }

    public EventSlot withAssignedUser(UserKey userKey, PositionKey positionKey) {
        return new EventSlot(order, required, positions, name, userKey, positionKey);
    }
}
