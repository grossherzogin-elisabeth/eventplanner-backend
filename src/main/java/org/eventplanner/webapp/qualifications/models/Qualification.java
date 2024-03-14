package org.eventplanner.webapp.qualifications.models;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record Qualification(
        @NonNull QualificationKey key,
        @NonNull String name,
        @Nullable String description,
        boolean expires
) {
}