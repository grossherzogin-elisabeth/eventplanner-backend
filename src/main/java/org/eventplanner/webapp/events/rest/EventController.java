package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.events.EventService;
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
@RequestMapping("/api/v1/events")
@EnableMethodSecurity(securedEnabled = true)
public class EventController {

    private final EventService eventService;

    public EventController(@Autowired EventService eventService) {
        this.eventService = eventService;
    }

    @Secured(Role.ADMIN)
    @RequestMapping(method = RequestMethod.POST, path = "")
    public ResponseEntity<EventRepresentation> createEvent() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Secured(Role.ANY)
    @RequestMapping(method = RequestMethod.GET, path = "/by-year/{year}")
    public ResponseEntity<List<EventRepresentation>> getEventsByYear(@PathVariable int year) {
        var events = this.eventService.getEvents(year)
                .stream()
                .map(EventRepresentation::fromDomain)
                .toList();
        return ResponseEntity.ok(events);
    }

    @Secured(Role.ADMIN)
    @RequestMapping(method = RequestMethod.PUT, path = "/by-key/{key}")
    public ResponseEntity<EventRepresentation> updateEvent(@PathVariable int year, @PathVariable String key) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Secured(Role.ADMIN)
    @RequestMapping(method = RequestMethod.DELETE, path = "/by-key/{key}")
    public ResponseEntity<Void> deleteEvent() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
