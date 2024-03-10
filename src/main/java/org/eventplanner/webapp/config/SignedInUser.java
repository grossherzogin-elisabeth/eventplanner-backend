package org.eventplanner.webapp.config;

import org.eventplanner.webapp.exceptions.MissingPermissionException;
import org.eventplanner.webapp.exceptions.NotImplementedException;
import org.eventplanner.webapp.exceptions.UnauthorizedException;
import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record SignedInUser(
        @NonNull UserKey key,
        @NonNull AuthKey authKey,
        @NonNull List<Role> roles,
        @NonNull List<Permission> permissions,
        @NonNull String email
) {

    private static final Logger log = LoggerFactory.getLogger(SignedInUser.class);

    public boolean isAnonymousUser() {
        return authKey.value().equals("anonymous");
    }

    public boolean hasPermission(@NonNull Permission permission) {
        return permissions.contains(permission);
    }

    public void assertHasPermission(@NonNull Permission permission) throws UnauthorizedException, MissingPermissionException {
        if (isAnonymousUser()) {
            throw new UnauthorizedException();
        }
        if (!hasPermission(permission)) {
            throw new MissingPermissionException();
        }
    }

    public void assertHasAnyPermission(@NonNull Permission... permissions) throws UnauthorizedException, MissingPermissionException {
        if (isAnonymousUser()) {
            throw new UnauthorizedException();
        }
        for (Permission permission : permissions) {
            if (hasPermission(permission)) {
                return;
            }
        }
        throw new MissingPermissionException();
    }

    public static @NonNull SignedInUser fromAuthentication(@Nullable Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException();
        }
        var permissions = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(Permission::fromString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        var roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(Role::fromString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            return new SignedInUser(
                    new UserKey("e6ff20e64a1d15ae"), // TODO
                    new AuthKey(oidcUser.getSubject()),
                    roles,
                    permissions,
                    oidcUser.getEmail()
            );
        }
        if (authentication.getPrincipal() instanceof OAuth2User) {
            log.error("Provided authentication is an OAuth2User, which is not implemented!");
            throw new NotImplementedException("TODO: Authentication type not implemented");
        }
        log.warn("Provided authentication is of unknown type: " + authentication.getClass().getName());
        throw new UnauthorizedException();
    }

    public static @NonNull SignedInUser technicalUser(Permission ...permissions) {
        return new SignedInUser(
                new UserKey("technical-user"),
                new AuthKey("technical-user"),
                List.of(Role.TECHNICAL_USER),
                List.of(permissions),
                "technical-user");
    }
}
