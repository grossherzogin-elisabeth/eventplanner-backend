package org.eventplanner.driving.rest.models;

import org.springframework.lang.NonNull;

import java.io.Serializable;

public record StatusRepresentation(
    @NonNull String buildCommit,
    @NonNull String buildBranch,
    @NonNull String buildDate
) implements Serializable {
}
