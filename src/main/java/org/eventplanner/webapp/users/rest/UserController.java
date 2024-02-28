package org.eventplanner.webapp.users.rest;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.users.UserService;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@EnableMethodSecurity(securedEnabled = true)
public class UserController {

    private final UserService userService;

    public UserController(@Autowired UserService userService) {
        this.userService = userService;
    }

//    @Secured(Role.ANY)
    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<List<UserRepresentation>> getUsers() {
        var users = userService.getUsers().stream()
                .map(UserRepresentation::fromDomain)
                .toList();
        return ResponseEntity.ok(users);
    }

    @Secured(Role.ADMIN)
    @RequestMapping(method = RequestMethod.GET, path = "/by-key/{key}")
    public ResponseEntity<UserDetailsRepresentation> getUserByKey(@PathVariable String key) {
        return userService.getUserByKey(new UserKey(key))
                .map(UserDetailsRepresentation::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
