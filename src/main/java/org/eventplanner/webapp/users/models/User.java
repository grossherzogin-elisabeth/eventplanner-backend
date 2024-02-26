package org.eventplanner.webapp.users.models;

import org.springframework.lang.NonNull;

public record User(
        @NonNull UserKey key,
        @NonNull AuthKey authKey,
        @NonNull String firstName,
        @NonNull String lastName,
        @NonNull String email
) {

}
