package org.eventplanner.webapp.events.rest.models;

import org.springframework.lang.NonNull;

import java.io.Serializable;

public record AddUserToWaitingListRequest(
    @NonNull String userKey,
    @NonNull String positionKey
) implements Serializable {
}
