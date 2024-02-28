package org.eventplanner.webapp.users;

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

    public @Nullable User getSignedInUser() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof DefaultOidcUser oidcUser) {
            var authKey = new AuthKey(oidcUser.getSubject());
            var user =  getUserByAuthKey(authKey);
            if (user.isPresent()) {
                return user.get();
            }
            log.warn(format("User with valid authorization key '%s' is not present in user db", authKey.value()));
            return new User(
                    new UserKey("?"),
                    authKey,
                    "?",
                    "?",
                    oidcUser.getEmail(),
                    new ArrayList<>());
        } else {
            return null;
        }
    }

    public @NonNull List<User> getUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByKey(@NonNull UserKey key) {
        return userRepository.findByKey(key);
    }

    public Optional<User> getUserByAuthKey(@NonNull AuthKey key) {
        return userRepository.findByAuthKey(key);
    }
}
