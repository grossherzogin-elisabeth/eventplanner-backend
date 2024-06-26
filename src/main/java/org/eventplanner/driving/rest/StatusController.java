package org.eventplanner.driving.rest;

import org.eventplanner.driving.rest.models.StatusRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/status")
@EnableMethodSecurity(securedEnabled = true)
public class StatusController {

    private final String buildCommit;
    private final String buildBranch;
    private final String buildTime;

    @Autowired
    public StatusController(
        @Value("${build.commit}") String buildCommit,
        @Value("${build.branch}") String buildBranch,
        @Value("${build.time}") String buildTime
    ) {
        this.buildCommit = buildCommit;
        this.buildBranch = buildBranch;
        this.buildTime = buildTime;
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<StatusRepresentation> getStatus() {
        return ResponseEntity.ok(new StatusRepresentation(
            buildCommit,
            buildBranch,
            buildTime
        ));
    }
}
