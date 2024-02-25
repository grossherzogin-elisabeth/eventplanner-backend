package org.eventplanner.webapp.positions.rest;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.positions.PositionService;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

//    @Secured(Role.ANY)
    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<List<PositionRepresentation>> getPositions() {
        var positions = this.positionService.getPosition().stream()
                .map(PositionRepresentation::fromDomain)
                .toList();
        return ResponseEntity.ok(positions);
    }
}
