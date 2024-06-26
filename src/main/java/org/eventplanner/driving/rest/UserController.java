package org.eventplanner.driving.rest;

import org.eventplanner.domain.users.models.UserKey;
import org.eventplanner.driving.rest.models.UserDetailsRepresentation;
import org.eventplanner.driving.rest.models.UserRepresentation;
import org.eventplanner.domain.users.UserUseCase;
import org.eventplanner.domain.users.models.UserKey;
import org.eventplanner.driving.rest.models.UserDetailsRepresentation;
import org.eventplanner.driving.rest.models.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@EnableMethodSecurity(securedEnabled = true)
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(@Autowired UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<List<UserRepresentation>> getUsers() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var users = userUseCase.getUsers(signedInUser).stream()
            .map(UserRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(users);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/by-key/{key}")
    public ResponseEntity<UserDetailsRepresentation> getUserByKey(@PathVariable("key") String key) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        return userUseCase.getUserByKey(signedInUser, new UserKey(key))
            .map(UserDetailsRepresentation::fromDomain)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
