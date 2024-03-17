package org.eventplanner.webapp.users;

import org.eventplanner.webapp.config.Permission;
import org.eventplanner.webapp.config.SignedInUser;
import org.eventplanner.webapp.exceptions.NotImplementedException;
import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.UserDetails;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(@Autowired UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public @NonNull List<UserDetails> getUsers(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_USERS);

        return userRepository.findAll();
    }

    public @NonNull List<org.springframework.security.core.userdetails.UserDetails> getDetailedUsers(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);

        throw new NotImplementedException("User details are not yet implemented");
    }

    public Optional<UserDetails> getUserByKey(@NonNull SignedInUser signedInUser, @NonNull UserKey key) {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);

        return userRepository.findByKey(key);
    }

    public Optional<UserDetails> getUserByAuthKey(@NonNull SignedInUser signedInUser, @NonNull AuthKey key) {
        signedInUser.assertHasPermission(Permission.READ_USERS);

        return userRepository.findByAuthKey(key);
    }
}
