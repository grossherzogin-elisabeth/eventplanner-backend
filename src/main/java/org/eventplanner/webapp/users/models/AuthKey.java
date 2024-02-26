package org.eventplanner.webapp.users.models;

import org.springframework.lang.NonNull;

public record AuthKey(
        @NonNull String value
) {
}
