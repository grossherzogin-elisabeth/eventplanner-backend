package org.eventplanner.webapp.users;

import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.UserDetails;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    @NonNull List<UserDetails> findAll();

    @NonNull Optional<UserDetails> findByKey(@NonNull UserKey key);

    @NonNull Optional<UserDetails> findByAuthKey(@NonNull AuthKey key);

    @NonNull
    UserDetails create(@NonNull UserDetails user);

    void deleteAll();
}
