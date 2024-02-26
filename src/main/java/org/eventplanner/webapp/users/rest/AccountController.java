package org.eventplanner.webapp.users.rest;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
@EnableMethodSecurity(securedEnabled = true)
public class AccountController {

    private final UserService userService;

    public AccountController(@Autowired UserService userService) {
        this.userService = userService;
    }

    @Secured(Role.ANY)
    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<AccountRepresentation> getSignedInUser() {
        var user = this.userService.getSignedInUser();
        if (user != null) {
            return ResponseEntity.ok(new AccountRepresentation(user.authKey().value(), user.email()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
