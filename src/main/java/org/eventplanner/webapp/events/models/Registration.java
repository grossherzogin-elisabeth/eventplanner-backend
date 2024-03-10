package org.eventplanner.webapp.events.models;

import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record Registration(
        @NonNull PositionKey position,
        @Nullable UserKey user,
        @Nullable String name,
        @Nullable SlotKey slot
) {

    public static Registration ofUser(@NonNull UserKey user, @NonNull PositionKey position) {
        return new Registration(position, user, null, null);
    }

    public static Registration ofPerson(@NonNull String name, @NonNull PositionKey position) {
        return new Registration(position, null, name, null);
    }

    public Registration withSlot(@NonNull SlotKey slot) {
        return new Registration(position, user, name, slot);
    }
}
