package org.eventplanner.webapp.users.filesystem;

import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.Role;
import org.eventplanner.webapp.users.models.UserDetails;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.eventplanner.webapp.utils.ObjectUtils.mapNullable;

public record UserDetailsJsonEntity(
        @NonNull String key,
        @Nullable String authKey,
        @Nullable String title,
        @NonNull String firstName,
        @Nullable String secondName,
        @NonNull String lastName,
        @Nullable List<String> positions,
        @Nullable List<String> roles,
        @Nullable List<UserQualificationJsonEntity> qualifications,
        @Nullable AddressJsonEntity address,
        @Nullable String email,
        @Nullable String phone,
        @Nullable String mobile,
        @Nullable String dateOfBirth,
        @Nullable String placeOfBirth,
        @Nullable String passNr,
        @Nullable String comment
) implements Serializable {

    public static @NonNull UserDetailsJsonEntity fromDomain(@NonNull UserDetails user) {
        return new UserDetailsJsonEntity(
                user.key().value(),
                mapNullable(user.authKey(), AuthKey::value),
                user.title(),
                user.firstName(),
                user.secondName(),
                user.lastName(),
                user.positions().stream().map(PositionKey::value).toList(),
                user.roles().stream().map(Role::value).toList(),
                user.qualifications().stream().map(UserQualificationJsonEntity::fromDomain).toList(),
                mapNullable(user.address(), AddressJsonEntity::fromDomain),
                user.email(),
                user.phone(),
                user.mobile(),
                mapNullable(user.dateOfBirth(), ZonedDateTime::toString),
                user.placeOfBirth(),
                user.passNr(),
                user.comment()
        );
    }

    public UserDetails toDomain() {
        return new UserDetails(
                new UserKey(key),
                mapNullable(authKey, AuthKey::new),
                title,
                firstName,
                secondName,
                lastName,
                mapNullable(positions, PositionKey::new, Collections.emptyList()),
                mapNullable(roles, Role::fromString, Collections.emptyList())
                        .stream().flatMap(Optional::stream).toList(),
                mapNullable(qualifications, UserQualificationJsonEntity::toDomain, Collections.emptyList()),
                mapNullable(address, AddressJsonEntity::toDomain),
                email,
                phone,
                mobile,
                mapNullable(dateOfBirth, ZonedDateTime::parse),
                placeOfBirth,
                passNr,
                comment
        );
    }
}
