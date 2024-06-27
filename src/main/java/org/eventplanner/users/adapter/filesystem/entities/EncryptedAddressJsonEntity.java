package org.eventplanner.users.adapter.filesystem.entities;

import org.eventplanner.users.entities.Address;
import org.eventplanner.users.adapter.filesystem.Crypto;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record EncryptedAddressJsonEntity(
    @NonNull String street,
    @NonNull String town,
    @NonNull String zipcode
) implements Serializable {

    public static @NonNull EncryptedAddressJsonEntity fromDomain(
        @NonNull final Address domain,
        @NonNull final Crypto crypto
    ) {
        return new EncryptedAddressJsonEntity(
            crypto.encrypt(domain.street()),
            crypto.encrypt(domain.town()),
            crypto.encrypt(String.valueOf(domain.zipcode()))
        );
    }

    public @NonNull Address toDomain(@NonNull final Crypto crypto) {
        return new Address(
            crypto.decrypt(street),
            crypto.decrypt(town),
            Integer.parseInt(crypto.decrypt(zipcode))
        );
    }
}
