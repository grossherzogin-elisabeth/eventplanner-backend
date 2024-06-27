package org.eventplanner.users.adapter.filesystem.entities;

import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.entities.UserQualification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;

public record EncryptedUserQualificationJsonEntity(
    @NonNull String qualificationKey,
    @Nullable String expiresAt
) implements Serializable {

    public static @NonNull EncryptedUserQualificationJsonEntity fromDomain(@NonNull UserQualification domain) {
        return new EncryptedUserQualificationJsonEntity(
            domain.qualificationKey().value(),
            domain.expiresAt() != null ? domain.expiresAt().toString() : null);
    }

    public @NonNull UserQualification toDomain() {
        return new UserQualification(
            new QualificationKey(qualificationKey),
            expiresAt != null ? ZonedDateTime.parse(expiresAt) : null);
    }
}
