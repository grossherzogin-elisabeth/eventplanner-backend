package org.eventplanner.webapp.events.models;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EventLocation(
        @NonNull String name,
        @NonNull String icon,
        @Nullable String address,
        @Nullable String country
) {
}
