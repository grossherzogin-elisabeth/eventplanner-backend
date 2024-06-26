package org.eventplanner.driven.filesystem.events.models;

import org.eventplanner.domain.positions.models.PositionKey;
import org.eventplanner.domain.users.models.UserKey;
import org.eventplanner.domain.events.models.Registration;
import org.eventplanner.domain.events.models.SlotKey;
import org.eventplanner.domain.positions.models.PositionKey;
import org.eventplanner.domain.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

import static org.eventplanner.domain.utils.ObjectUtils.mapNullable;

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