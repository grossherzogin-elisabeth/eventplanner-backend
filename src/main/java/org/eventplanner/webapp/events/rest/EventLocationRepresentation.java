package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.events.models.EventLocation;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public record EventLocationRepresentation(
        @NonNull String name,
        @NonNull String icon,
        @Nullable String address,
        @Nullable String country
) implements Serializable {
    public static @NonNull EventLocationRepresentation fromDomain(@NonNull EventLocation domain) {
        return new EventLocationRepresentation(
                domain.name(),
                domain.icon(),
                domain.address(),
                domain.country());
    }

    public @NonNull EventLocation toDomain() {
        return new EventLocation(
                name,
                icon,
                address,
                country);
    }
}
