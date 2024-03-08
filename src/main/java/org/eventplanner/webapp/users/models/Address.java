package org.eventplanner.webapp.users.models;

import org.springframework.lang.NonNull;

public record Address(
        @NonNull String street,
        @NonNull String town,
        @NonNull int zipcode
) {
}
