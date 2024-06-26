package org.eventplanner.driving.rest.models;

import org.eventplanner.domain.users.models.UserDetails;
import org.eventplanner.domain.users.models.UserDetails;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public record UserDetailsRepresentation(
    @NonNull String key,
    @Nullable String authKey,
    @NonNull String firstName,
    @NonNull String lastName
) implements Serializable {
    public static UserDetailsRepresentation fromDomain(@NonNull UserDetails user) {
        return new UserDetailsRepresentation(
            user.key().value(),
            user.authKey() != null ? user.authKey().value() : null,
            user.firstName(),
            user.lastName()
        );
    }
}
