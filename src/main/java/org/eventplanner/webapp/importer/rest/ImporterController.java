package org.eventplanner.webapp.importer.rest;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.config.SignedInUser;
import org.eventplanner.webapp.importer.ImporterService;
import org.eventplanner.webapp.importer.models.ImportError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/import")
@EnableMethodSecurity(securedEnabled = true)
public class ImporterController {

    private final ImporterService importerService;

    public ImporterController(@Autowired ImporterService importerService) {
        this.importerService = importerService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/{year}")
    public ResponseEntity<List<ImportErrorRepresentation>> importEvents(@PathVariable int year, @RequestParam("file") MultipartFile file) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());

        try (var stream = file.getInputStream()) {
            var errors = this.importerService.importEvents(signedInUser, year, stream).stream()
                    .map(ImportErrorRepresentation::fromDomain)
                    .toList();
            return ResponseEntity.ok(errors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/users")
    public ResponseEntity<Void> importUsers(@RequestParam("file") MultipartFile file) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());

        try (var stream = file.getInputStream()) {
            this.importerService.importUsers(signedInUser, stream);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
