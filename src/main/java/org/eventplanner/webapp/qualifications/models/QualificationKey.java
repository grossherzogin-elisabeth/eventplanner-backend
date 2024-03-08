package org.eventplanner.webapp.qualifications.models;

import org.springframework.lang.NonNull;

public record QualificationKey(
        @NonNull String value
) {
}
