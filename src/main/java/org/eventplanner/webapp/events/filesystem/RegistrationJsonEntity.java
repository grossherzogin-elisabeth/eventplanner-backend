package org.eventplanner.webapp.events.filesystem;

import org.eventplanner.webapp.events.models.Registration;
import org.eventplanner.webapp.events.models.SlotKey;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

import static org.eventplanner.webapp.utils.ObjectUtils.mapNullable;

public record RegistrationJsonEntity(
        @NonNull String positionKey,
        @Nullable String userKey,
        @Nullable String name,
        @Nullable String slotKey
) implements Serializable {

    public static @NonNull RegistrationJsonEntity fromDomain(@NonNull Registration domain) {
        return new RegistrationJsonEntity(
                domain.position().value(),
                mapNullable(domain.user(), UserKey::value),
                domain.name(),
                mapNullable(domain.slot(), SlotKey::value));
    }

    public @NonNull Registration toDomain() {
        return new Registration(
                new PositionKey(positionKey),
                mapNullable(userKey, UserKey::new),
                name,
                mapNullable(slotKey, SlotKey::new));
    }
}
