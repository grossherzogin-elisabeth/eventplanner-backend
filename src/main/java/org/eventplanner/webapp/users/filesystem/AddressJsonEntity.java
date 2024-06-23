package org.eventplanner.webapp.users.filesystem;

import org.eventplanner.webapp.users.models.Address;
import org.eventplanner.webapp.utils.Crypto;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record AddressJsonEntity(
    @NonNull String street,
    @NonNull String town,
    @NonNull String zipcode
) implements Serializable {

    public static @NonNull AddressJsonEntity fromDomain(
        @NonNull final Address domain,
        @NonNull final Crypto crypto
    ) {
        return new AddressJsonEntity(
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
