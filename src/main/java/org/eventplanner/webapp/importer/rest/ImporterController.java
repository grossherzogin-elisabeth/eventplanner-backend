package org.eventplanner.webapp.importer.rest;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.importer.ImporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/import")
@EnableMethodSecurity(securedEnabled = true)
public class ImporterController {

    private final ImporterService importerService;

    public ImporterController(@Autowired ImporterService importerService) {
        this.importerService = importerService;
    }

    @Secured(Role.ADMIN)
    @RequestMapping(method = RequestMethod.POST, value = "/events/{year}")
    public ResponseEntity<Void> importEvents(@PathVariable int year, @RequestParam("file") MultipartFile file) {
        try (var stream = file.getInputStream()) {
            this.importerService.importFile(stream, "events-"+year+".xlsx");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Secured(Role.ADMIN)
    @RequestMapping(method = RequestMethod.POST, value = "/users")
    public ResponseEntity<Void> importUsers(@RequestParam("file") MultipartFile file) {
        try (var stream = file.getInputStream()) {
            this.importerService.importFile(stream, "users.xlsx");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
