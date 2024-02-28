package org.eventplanner.webapp.users.rest;

import org.eventplanner.webapp.users.models.User;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record AccountRepresentation(
        @NonNull String key,
        @NonNull String email
) implements Serializable {
    public static AccountRepresentation fromDomain(@NonNull User user) {
        return new AccountRepresentation(
                user.key().value(),
                user.email()
        );
    }
}
