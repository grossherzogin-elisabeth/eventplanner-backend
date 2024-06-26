package org.eventplanner.domain.users.models;

import org.springframework.lang.NonNull;

public record Address(
    @NonNull String street,
    @NonNull String town,
    int zipcode
) {
}
