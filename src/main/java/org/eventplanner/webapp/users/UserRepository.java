package org.eventplanner.webapp.users;

import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.User;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    @NonNull List<User> findAll();

    @NonNull Optional<User> findByKey(@NonNull UserKey key);

    @NonNull Optional<User> findByAuthKey(@NonNull AuthKey key);
}
