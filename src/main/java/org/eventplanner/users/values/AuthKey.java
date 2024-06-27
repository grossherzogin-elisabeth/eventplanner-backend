package org.eventplanner.users.values;

import org.springframework.lang.NonNull;

public record AuthKey(
    @NonNull String value
) {
}
