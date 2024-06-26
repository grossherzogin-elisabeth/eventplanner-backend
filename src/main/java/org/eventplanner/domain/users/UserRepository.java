package org.eventplanner.domain.users;

import org.eventplanner.domain.users.models.AuthKey;
import org.eventplanner.domain.users.models.UserDetails;
import org.eventplanner.domain.users.models.UserKey;
import org.eventplanner.domain.users.models.AuthKey;
import org.eventplanner.domain.users.models.UserDetails;
import org.eventplanner.domain.users.models.UserKey;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    @NonNull
    List<UserDetails> findAll();

    @NonNull
    Optional<UserDetails> findByKey(@NonNull UserKey key);

    @NonNull
    Optional<UserDetails> findByAuthKey(@NonNull AuthKey key);

    @NonNull
    Optional<UserDetails> findByEmail(@NonNull String email);

    @NonNull
    Optional<UserDetails> findByName(@NonNull String firstName, @NonNull String lastName);

    @NonNull
    UserDetails create(@NonNull UserDetails user);

    @NonNull
    UserDetails update(@NonNull UserDetails user);

    void deleteAll();
}
