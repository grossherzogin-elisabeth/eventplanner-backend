package org.eventplanner.webapp.users.filesystem;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.UserDetails;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public record UserJsonEntity(
        @NonNull String key,
        @Nullable String authKey,
        @NonNull String firstName,
        @NonNull String lastName,
        @NonNull String email,
        @NonNull List<String> positions,
        @NonNull List<String> roles
) implements Serializable {

    public static UserJsonEntity fromDomain(@NonNull UserDetails user) {
        return new UserJsonEntity(
                user.key().value(),
                user.authKey() != null ? user.authKey().value() : null,
                user.firstName(),
                user.lastName(),
                user.email(),
                user.positions().stream().map(PositionKey::value).toList(),
                user.roles().stream().map(Role::value).toList()
        );
    }

    public UserDetails toDomain() {
        return new UserDetails(
                new UserKey(key),
                firstName,
                lastName,
                positions.stream().map(PositionKey::new).toList(),
                roles.stream()
                        .map(Role::fromString)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList()
        );
    }
}
