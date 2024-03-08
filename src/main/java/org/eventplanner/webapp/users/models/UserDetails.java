package org.eventplanner.webapp.users.models;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.qualifications.models.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public record UserDetails(
        @NonNull UserKey key,
        @Nullable AuthKey authKey,
        @NonNull String firstName,
        @NonNull String lastName,
        @NonNull List<PositionKey> positions,
        @NonNull List<Role> roles,
        @NonNull List<UserQualification> qualifications,
        @Nullable Address address,
        @Nullable String email,
        @Nullable String phone,
        @Nullable String mobile,
        @Nullable Instant dateOfBirth,
        @Nullable String placeOfBirth,
        @Nullable String passNr,
        @Nullable String comment
) {

    public UserDetails(
            @NonNull UserKey key,
            @NonNull String firstName,
            @NonNull String lastName,
            @NonNull List<PositionKey> positions,
            @NonNull List<Role> roles
    ) {
        this(
                key,
                null,
                firstName,
                lastName,
                positions,
                roles,
                Collections.emptyList(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public UserDetails withAddPosition(PositionKey position) {
        var positions = new HashSet<>(positions());
        positions.add(position);
        return new UserDetails(
                key,
                authKey,
                firstName,
                lastName,
                positions.stream().toList(),
                roles,
                qualifications,
                address,
                email,
                phone,
                mobile,
                dateOfBirth,
                placeOfBirth,
                passNr,
                comment
        );
    }

    public UserDetails withAddQualification(QualificationKey qualification, Instant expires) {
        var qualifications = new ArrayList<>(qualifications());
        qualifications.add(new UserQualification(qualification, expires));
        return new UserDetails(
                key,
                authKey,
                firstName,
                lastName,
                positions,
                roles,
                qualifications,
                address,
                email,
                phone,
                mobile,
                dateOfBirth,
                placeOfBirth,
                passNr,
                comment
        );
    }
}
