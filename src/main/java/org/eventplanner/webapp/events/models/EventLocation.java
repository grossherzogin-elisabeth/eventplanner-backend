package org.eventplanner.webapp.events.models;

import org.springframework.lang.NonNull;

public record EventLocation(
        @NonNull String name,
        @NonNull String icon
) {
}
