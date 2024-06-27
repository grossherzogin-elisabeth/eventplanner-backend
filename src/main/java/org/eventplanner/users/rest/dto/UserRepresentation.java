package org.eventplanner.users.rest.dto;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserDetails;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public record UserRepresentation(
    @NonNull String key,
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull List<String> positions
) implements Serializable {
    public static UserRepresentation fromDomain(@NonNull UserDetails user) {
        return new UserRepresentation(
            user.key().value(),
            user.firstName(),
            user.lastName(),
            user.positions().stream().map(PositionKey::value).toList()
        );
    }
}
