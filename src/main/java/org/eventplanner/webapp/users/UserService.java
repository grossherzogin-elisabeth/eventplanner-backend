package org.eventplanner.webapp.users;

import org.eventplanner.webapp.config.Permission;
import org.eventplanner.webapp.config.SignedInUser;
import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.User;
import org.eventplanner.webapp.users.models.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;

    public UserService(@Autowired UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public @NonNull User getSignedInUser(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_OWN_USER);

        var user =  userRepository.findByAuthKey(signedInUser.authKey());
        if (user.isPresent()) {
            return user.get();
        }
        log.warn(format("User with valid authorization key '%s' is not present in user db", signedInUser.authKey().value()));
        // TODO get actual user
        return new User(
                new UserKey("e6ff20e64a1d15ae"),
                signedInUser.authKey(),
                "Malte",
                "Schwitters",
                signedInUser.email(),
                new ArrayList<>(),
                signedInUser.roles());
    }

    public @NonNull List<User> getUsers(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_USERS);

        return userRepository.findAll();
    }

    public Optional<User> getUserByKey(@NonNull SignedInUser signedInUser, @NonNull UserKey key) {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);

        return userRepository.findByKey(key);
    }

    public Optional<User> getUserByAuthKey(@NonNull SignedInUser signedInUser, @NonNull AuthKey key) {
        signedInUser.assertHasPermission(Permission.READ_USERS);

        return userRepository.findByAuthKey(key);
    }
}
