package org.eventplanner.domain.users.models;

import org.eventplanner.domain.qualifications.QualificationKey;
import org.eventplanner.domain.qualifications.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;

public record UserQualification(
    @NonNull QualificationKey qualificationKey,
    @Nullable ZonedDateTime expiresAt
) {
}
