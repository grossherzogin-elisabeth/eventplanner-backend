package org.eventplanner.webapp.users.rest;

import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.User;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Optional;

public record UserDetailsRepresentation(
        @NonNull String key,
        @Nullable String authKey,
        @NonNull String firstName,
        @NonNull String lastName
) implements Serializable {
    public static UserDetailsRepresentation fromDomain(@NonNull User user) {
        return new UserDetailsRepresentation(
                user.key().value(),
                user.authKey() != null ? user.authKey().value() : null,
                user.firstName(),
                user.lastName()
        );
    }
}
