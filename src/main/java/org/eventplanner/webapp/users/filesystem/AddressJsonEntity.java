package org.eventplanner.webapp.users.filesystem;

import org.eventplanner.webapp.users.models.Address;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public record AddressJsonEntity(
        @NonNull String street,
        @NonNull String town,
        @NonNull int zipcode
) implements Serializable {

    public static @Nullable AddressJsonEntity fromDomainNullable(@Nullable Address domain) {
        if (domain == null) {
            return null;
        }
        return new AddressJsonEntity(domain.street(), domain.town(), domain.zipcode());
    }

    public static @NonNull AddressJsonEntity fromDomain(@NonNull Address domain) {
        return new AddressJsonEntity(domain.street(), domain.town(), domain.zipcode());
    }

    public @NonNull Address toDomain() {
        return new Address(street, town, zipcode);
    }
}
