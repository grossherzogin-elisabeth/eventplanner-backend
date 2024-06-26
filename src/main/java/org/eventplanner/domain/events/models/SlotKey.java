package org.eventplanner.domain.events.models;

import org.springframework.lang.NonNull;

public record SlotKey(
    @NonNull String value
) {
}
