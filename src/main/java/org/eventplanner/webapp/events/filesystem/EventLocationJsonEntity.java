package org.eventplanner.webapp.events.filesystem;

import org.eventplanner.webapp.events.models.EventLocation;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public record EventLocationJsonEntity(
        @Nullable String name,
        @Nullable String icon,
        @Nullable String address,
        @Nullable String country
) implements Serializable {

    public static @NonNull EventLocationJsonEntity fromDomain(@NonNull EventLocation domain) {
        return new EventLocationJsonEntity(
                domain.name(),
                domain.icon(),
                domain.address(),
                domain.country());
    }

    public EventLocation toDomain() {
        return new EventLocation(
                name != null ? name : "",
                icon != null ? icon : "",
                address,
                country);
    }
}
