package org.eventplanner.users.entities;

import org.eventplanner.qualifications.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;

public record UserQualification(
    @NonNull QualificationKey qualificationKey,
    @Nullable ZonedDateTime expiresAt
) {
}
