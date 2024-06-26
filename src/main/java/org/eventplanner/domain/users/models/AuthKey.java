package org.eventplanner.domain.users.models;

import org.springframework.lang.NonNull;

public record AuthKey(
    @NonNull String value
) {
}
