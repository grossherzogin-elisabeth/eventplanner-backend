package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.config.SignedInUser;
import org.eventplanner.webapp.events.EventService;
import org.eventplanner.webapp.events.models.EventKey;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
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
@RequestMapping("/api/v1/events")
@EnableMethodSecurity(securedEnabled = true)
public class EventController {

    private final EventService eventService;

    public EventController(@Autowired EventService eventService) {
        this.eventService = eventService;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/by-year/{year}")
    public ResponseEntity<List<EventRepresentation>> getEventsByYear(@PathVariable int year) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        var events = eventService.getEvents(signedInUser, year)
                .stream()
                .map(EventRepresentation::fromDomain)
                .toList();
        return ResponseEntity.ok(events);
    }

    @RequestMapping(method = RequestMethod.POST, path = "")
    public ResponseEntity<EventRepresentation> createEvent(@RequestBody CreateEventRequest spec) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        var event = eventService.createEvent(signedInUser, spec.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/by-key/{eventKey}")
    public ResponseEntity<EventRepresentation> updateEvent(@PathVariable String eventKey, @RequestBody UpdateEventRequest spec) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        var event = eventService.updateEvent(signedInUser, new EventKey(eventKey), spec.toDomain());
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/by-key/{eventKey}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String eventKey) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        eventService.deleteEvent(signedInUser, new EventKey(eventKey));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/by-key/{eventKey}/waitinglist")
    public ResponseEntity<EventRepresentation> addUserToWaitingList(@PathVariable String eventKey, @RequestBody AddUserToWaitingListRequest spec) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        var event = eventService.addUserToWaitingList(
                signedInUser,
                new EventKey(eventKey),
                new UserKey(spec.userKey()),
                new PositionKey(spec.positionKey()));
        return ResponseEntity.status(HttpStatus.CREATED).body(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/by-key/{eventKey}/waitinglist/{userKey}")
    public ResponseEntity<EventRepresentation> removeUserFromWaitingList(@PathVariable String eventKey, @PathVariable String userKey) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        var event = eventService.removeUserFromWaitingList(
                signedInUser,
                new EventKey(eventKey),
                new UserKey(userKey));
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/by-key/{eventKey}/registrations")
    public ResponseEntity<EventRepresentation> updateEventTeam(@PathVariable String eventKey, @RequestBody UpdateEventTeamRequest spec) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        var event = eventService.updateEventTeam(
                signedInUser,
                new EventKey(eventKey),
                spec.toDomain());
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/by-key/{eventKey}/registrations/{userKey}")
    public ResponseEntity<EventRepresentation> removeUserFromEventTeam(@PathVariable String eventKey, @PathVariable String userKey) {
        var signedInUser = SignedInUser.fromAuthentication(SecurityContextHolder.getContext().getAuthentication());
        var event = eventService.removeUserFromTeam(
                signedInUser,
                new EventKey(eventKey),
                new UserKey(userKey));
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }
}
