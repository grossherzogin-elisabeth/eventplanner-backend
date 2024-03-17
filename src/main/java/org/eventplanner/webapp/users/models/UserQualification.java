package org.eventplanner.webapp.users.models;

import org.eventplanner.webapp.qualifications.models.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;

public record UserQualification(
        @NonNull QualificationKey qualificationKey,
        @Nullable ZonedDateTime expiresAt
) {
}
