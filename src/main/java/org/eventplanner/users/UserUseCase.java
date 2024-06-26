package org.eventplanner.users;

import io.micrometer.common.lang.Nullable;
import org.eventplanner.users.adapter.UserRepository;
import org.eventplanner.users.entities.*;
import org.eventplanner.exceptions.NotImplementedException;
import org.eventplanner.exceptions.UnauthorizedException;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Permission;
import org.eventplanner.users.values.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class UserUseCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;

    public UserUseCase(@Autowired UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public @NonNull SignedInUser getSignedInUser(@Nullable Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException();
        }
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            var authkey = new AuthKey(oidcUser.getSubject());
            var maybeUser = userRepository.findByAuthKey(authkey);
            if (maybeUser.isPresent()) {
                return SignedInUser
                    .fromUser(maybeUser.get())
                    .withPermissionsFromAuthentication(authentication);
            }
            maybeUser = userRepository.findByEmail(oidcUser.getEmail());
            if (maybeUser.isPresent()) {
                var user = maybeUser.get();
                user = userRepository.update(user.withAuthKey(authkey));
                return SignedInUser
                    .fromUser(user)
                    .withPermissionsFromAuthentication(authentication);
            }

            var firstName = oidcUser.getAttributes().get("given_name");
            var lastName = oidcUser.getAttributes().get("family_name");
            if (firstName != null && lastName != null) {
                maybeUser = userRepository.findByName(firstName.toString(), lastName.toString());
                if (maybeUser.isPresent()) {
                    var user = maybeUser.get();
                    user = userRepository.update(user.withAuthKey(authkey));
                    return SignedInUser
                        .fromUser(user)
                        .withPermissionsFromAuthentication(authentication);
                }
            }

            return new SignedInUser(
                new UserKey("unknown"),
                authkey,
                Collections.emptyList(),
                Collections.emptyList(),
                oidcUser.getEmail()
            ).withPermissionsFromAuthentication(authentication);
        }
        if (authentication.getPrincipal() instanceof OAuth2User) {
            log.error("Provided authentication is an OAuth2User, which is not implemented!");
            throw new UnauthorizedException();
        }
        log.error("Authentication is of unknown type: {}", authentication.getClass().getName());
        throw new UnauthorizedException();
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

    public Optional<UserDetails> getUserByEmail(@NonNull SignedInUser signedInUser, @NonNull String email) {
        signedInUser.assertHasPermission(Permission.READ_USERS);

        return userRepository.findByEmail(email);
    }
}
