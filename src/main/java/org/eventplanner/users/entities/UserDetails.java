package org.eventplanner.users.entities;

import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Role;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public record UserDetails(
    @NonNull UserKey key,
    @Nullable AuthKey authKey,
    @Nullable String title,
    @NonNull String firstName,
    @Nullable String secondName,
    @NonNull String lastName,
    @NonNull List<PositionKey> positions,
    @NonNull List<Role> roles,
    @NonNull List<UserQualification> qualifications,
    @Nullable Address address,
    @Nullable String email,
    @Nullable String phone,
    @Nullable String mobile,
    @Nullable ZonedDateTime dateOfBirth,
    @Nullable String placeOfBirth,
    @Nullable String passNr,
    @Nullable String comment
) {

    public @NonNull String fullName() {
        StringBuilder stb = new StringBuilder();
        if (title != null) {
            stb.append(title).append(" ");
        }
        stb.append(firstName).append(" ");
        if (secondName != null) {
            stb.append(secondName).append(" ");
        }
        stb.append(lastName);
        return stb.toString();
    }

    public UserDetails withAuthKey(AuthKey authKey) {
        return new UserDetails(
            key,
            authKey,
            title,
            firstName,
            secondName,
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

    public UserDetails withAddPosition(PositionKey position) {
        var positions = new HashSet<>(positions());
        positions.add(position);
        return new UserDetails(
            key,
            authKey,
            title,
            firstName,
            secondName,
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

    public UserDetails withAddQualification(QualificationKey qualification, ZonedDateTime expires) {
        var qualifications = new ArrayList<>(qualifications());
        qualifications.add(new UserQualification(qualification, expires));
        return new UserDetails(
            key,
            authKey,
            title,
            firstName,
            secondName,
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
