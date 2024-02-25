package org.eventplanner.webapp.positions.rest;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.events.rest.EventRepresentation;
import org.eventplanner.webapp.positions.PositionService;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/positions")
@EnableMethodSecurity(securedEnabled = true)
public class PositionController {

    private final PositionService positionService;

    public PositionController(@Autowired PositionService positionService) {
        this.positionService = positionService;
    }

    @Secured(Role.ADMIN)
    @RequestMapping(method = RequestMethod.POST, path = "")
    public ResponseEntity<EventRepresentation> createPosition() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Secured(Role.ANY)
    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<List<PositionRepresentation>> getPositions() {
        var positions = this.positionService.getPosition().stream()
                .map(PositionRepresentation::fromDomain)
                .toList();
        return ResponseEntity.ok(positions);
    }

    @Secured(Role.ADMIN)
    @RequestMapping(method = RequestMethod.PUT, path = "/{key}")
    public ResponseEntity<EventRepresentation> updatePosition(@PathVariable String key, @RequestBody EventRepresentation spec) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Secured(Role.ADMIN)
    @RequestMapping(method = RequestMethod.DELETE, path = "/{key}")
    public ResponseEntity<EventRepresentation> deletePosition(@PathVariable String key) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
