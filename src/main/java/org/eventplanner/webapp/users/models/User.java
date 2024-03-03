package org.eventplanner.webapp.users.models;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public record User(
        @NonNull UserKey key,
        @Nullable AuthKey authKey,
        @NonNull String firstName,
        @NonNull String lastName,
        @NonNull String email,
        @NonNull List<PositionKey> positions,
        @NonNull List<Role> roles
) {

}
