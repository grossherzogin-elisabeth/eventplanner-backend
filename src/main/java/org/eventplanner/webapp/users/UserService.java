package org.eventplanner.webapp.users;

import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.User;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public @NonNull User getSignedInUser() {
        var user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user instanceof DefaultOidcUser) {
            var oidcUser = (DefaultOidcUser) user;
            var authKey = oidcUser.getSubject();
            var email = oidcUser.getEmail();
            return new User(
                    new UserKey(authKey),
                    new AuthKey(authKey),
                    "",
                    "",
                    email);
        } else {
            return null;
        }
    }
}
