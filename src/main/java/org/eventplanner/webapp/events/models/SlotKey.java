package org.eventplanner.webapp.events.models;

import org.springframework.lang.NonNull;

public record SlotKey(
        @NonNull String value
) {
}
