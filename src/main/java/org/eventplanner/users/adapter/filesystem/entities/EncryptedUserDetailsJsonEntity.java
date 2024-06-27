package org.eventplanner.users.adapter.filesystem.entities;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Role;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.UserKey;
import org.eventplanner.users.adapter.filesystem.Crypto;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.eventplanner.utils.ObjectUtils.mapNullable;
import static org.eventplanner.utils.ObjectUtils.streamNullable;

public record EncryptedUserDetailsJsonEntity(
    @NonNull String key,
    @Nullable String authKey,
    @Nullable String title,
    @NonNull String firstName,
    @Nullable String secondName,
    @NonNull String lastName,
    @Nullable List<String> positions,
    @Nullable List<String> roles,
    @Nullable List<EncryptedUserQualificationJsonEntity> qualifications,
    @Nullable EncryptedAddressJsonEntity address,
    @Nullable String email,
    @Nullable String phone,
    @Nullable String mobile,
    @Nullable String dateOfBirth,
    @Nullable String placeOfBirth,
    @Nullable String passNr,
    @Nullable String comment
) implements Serializable {

    public static @NonNull EncryptedUserDetailsJsonEntity fromDomain(@NonNull final UserDetails user, @NonNull final Crypto crypto) {
        return new EncryptedUserDetailsJsonEntity(
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
            user.qualifications().stream().map(EncryptedUserQualificationJsonEntity::fromDomain).toList(),
            mapNullable(user.address(), (address) -> EncryptedAddressJsonEntity.fromDomain(address, crypto)),
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
            mapNullable(qualifications, EncryptedUserQualificationJsonEntity::toDomain, Collections.emptyList()),
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
