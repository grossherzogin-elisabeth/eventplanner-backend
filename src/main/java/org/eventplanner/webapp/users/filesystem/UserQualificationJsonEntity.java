package org.eventplanner.webapp.users.filesystem;

import org.eventplanner.webapp.qualifications.models.QualificationKey;
import org.eventplanner.webapp.users.models.UserQualification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;

public record UserQualificationJsonEntity(
        @NonNull String qualificationKey,
        @Nullable String expiresAt
) implements Serializable  {

    public static @NonNull UserQualificationJsonEntity fromDomain(@NonNull UserQualification domain) {
        return new UserQualificationJsonEntity(
                domain.qualificationKey().value(),
                domain.expiresAt() != null ? domain.expiresAt().toString() : null);
    }

    public @NonNull UserQualification toDomain() {
        return new UserQualification(
                new QualificationKey(qualificationKey),
                expiresAt != null ? ZonedDateTime.parse(expiresAt) : null);
    }
}
