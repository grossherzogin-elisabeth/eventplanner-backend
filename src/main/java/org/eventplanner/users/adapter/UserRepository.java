package org.eventplanner.users.adapter;

import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.UserKey;
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
