package org.eventplanner.driven.filesystem.users.models;

import org.eventplanner.domain.positions.models.PositionKey;
import org.eventplanner.domain.users.models.AuthKey;
import org.eventplanner.domain.users.models.Role;
import org.eventplanner.domain.users.models.UserDetails;
import org.eventplanner.domain.users.models.UserKey;
import org.eventplanner.driven.filesystem.users.Crypto;
import org.eventplanner.domain.positions.models.PositionKey;
import org.eventplanner.domain.users.models.AuthKey;
import org.eventplanner.domain.users.models.Role;
import org.eventplanner.domain.users.models.UserDetails;
import org.eventplanner.domain.users.models.UserKey;
import org.eventplanner.driven.filesystem.users.Crypto;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.eventplanner.domain.utils.ObjectUtils.mapNullable;
import static org.eventplanner.domain.utils.ObjectUtils.streamNullable;

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

    public static @NonNull UserDetailsJsonEntity fromDomain(@NonNull final UserDetails user, @NonNull final Crypto crypto) {
        return new UserDetailsJsonEntity(
            user.key().value(),
            mapNullable(user.authKey(), AuthKey::value),
            mapNullable(user.title(), crypto::encrypt),
            crypto.encrypt(user.firstName()),
            mapNullable(user.secondName(), crypto::encrypt),
            crypto.encrypt(user.lastName()),
            user.positions()
                .stream()
                .map(PositionKey::value)
                .map(crypto::encrypt)
                .toList(),
            user.roles().stream()
                .map(Role::value)
                .map(crypto::encrypt)
                .toList(),
            user.qualifications().stream().map(UserQualificationJsonEntity::fromDomain).toList(),
            mapNullable(user.address(), (address) -> AddressJsonEntity.fromDomain(address, crypto)),
            mapNullable(user.email(), crypto::encrypt),
            mapNullable(user.phone(), crypto::encrypt),
            mapNullable(user.mobile(), crypto::encrypt),
            ofNullable(user.dateOfBirth())
                .map(ZonedDateTime::toString)
                .map(crypto::encrypt)
                .orElse(null),
            mapNullable(user.placeOfBirth(), crypto::encrypt),
            mapNullable(user.passNr(), crypto::encrypt),
            mapNullable(user.comment(), crypto::encrypt)
        );
    }

    public UserDetails toDomain(@NonNull final Crypto crypto) {
        return new UserDetails(
            new UserKey(key),
            mapNullable(authKey, AuthKey::new),
            mapNullable(title, crypto::decrypt),
            crypto.decrypt(firstName),
            mapNullable(secondName, crypto::decrypt),
            crypto.decrypt(lastName),
            streamNullable(positions, Stream.empty())
                .map(crypto::decrypt)
                .map(PositionKey::new)
                .toList(),
            streamNullable(roles, Stream.empty())
                .map(crypto::decrypt)
                .map(Role::fromString)
                .flatMap(Optional::stream).toList(),
            mapNullable(qualifications, UserQualificationJsonEntity::toDomain, Collections.emptyList()),
            mapNullable(address, (json) -> json.toDomain(crypto)),
            mapNullable(email, crypto::decrypt),
            mapNullable(phone, crypto::decrypt),
            mapNullable(mobile, crypto::decrypt),
            ofNullable(dateOfBirth)
                .map(crypto::decrypt)
                .map(ZonedDateTime::parse)
                .orElse(null),
            mapNullable(placeOfBirth, crypto::decrypt),
            mapNullable(passNr, crypto::decrypt),
            mapNullable(comment, crypto::decrypt)
        );
    }
}
