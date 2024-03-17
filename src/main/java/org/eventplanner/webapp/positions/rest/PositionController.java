package org.eventplanner.webapp.positions.rest;

import org.eventplanner.webapp.config.SignedInUser;
import org.eventplanner.webapp.positions.PositionService;
import org.eventplanner.webapp.positions.models.Position;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @RequestMapping(method = RequestMethod.POST, path = "")
    public ResponseEntity<PositionRepresentation> createPosition(@RequestBody PositionRepresentation spec) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());

        var positionSpec = new Position(new PositionKey(""), spec.name(), spec.color(), spec.prio());
        var position = positionService.createPosition(signedInUser, positionSpec);
        return ResponseEntity.status(HttpStatus.CREATED).body(PositionRepresentation.fromDomain(position));
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<List<PositionRepresentation>> getPositions() {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());

        var positions = positionService.getPosition(signedInUser).stream()
                .map(PositionRepresentation::fromDomain)
                .toList();
        return ResponseEntity.ok(positions);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{positionKey}")
    public ResponseEntity<PositionRepresentation> updatePosition(@PathVariable String positionKey, @RequestBody PositionRepresentation spec) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());

        var positionSpec = new Position(new PositionKey(positionKey), spec.name(), spec.color(), spec.prio());
        var position = positionService.updatePosition(signedInUser, positionSpec.key(), positionSpec);
        return ResponseEntity.ok(PositionRepresentation.fromDomain(position));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{positionKey}")
    public ResponseEntity<Void> deletePosition(@PathVariable String positionKey) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());

        positionService.deletePosition(signedInUser, new PositionKey(positionKey));
        return ResponseEntity.ok().build();
    }
}
