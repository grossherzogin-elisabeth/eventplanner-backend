package org.eventplanner.users.entities;

import org.springframework.lang.NonNull;

public record Address(
    @NonNull String street,
    @NonNull String town,
    int zipcode
) {
}
