package org.eventplanner.driving.rest.models;

import org.eventplanner.domain.users.models.Permission;
import org.eventplanner.domain.users.models.Role;
import org.eventplanner.domain.users.models.SignedInUser;
import org.eventplanner.domain.users.models.Permission;
import org.eventplanner.domain.users.models.Role;
import org.eventplanner.domain.users.models.SignedInUser;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public record AccountRepresentation(
    @NonNull String key,
    @NonNull String email,
    @NonNull List<String> roles,
    @NonNull List<String> permissions
) implements Serializable {
    public static AccountRepresentation fromDomain(@NonNull SignedInUser signedInUser) {
        return new AccountRepresentation(
            signedInUser.key().value(),
            signedInUser.email(),
            signedInUser.roles().stream().map(Role::value).toList(),
            signedInUser.permissions().stream().map(Permission::value).toList()
        );
    }
}
