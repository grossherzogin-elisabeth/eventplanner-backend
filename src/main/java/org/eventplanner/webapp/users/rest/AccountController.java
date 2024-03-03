package org.eventplanner.webapp.users.rest;

import org.eventplanner.webapp.config.SignedInUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
@EnableMethodSecurity(securedEnabled = true)
public class AccountController {

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<AccountRepresentation> getSignedInUser() {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(AccountRepresentation.fromDomain(signedInUser));
    }
}
