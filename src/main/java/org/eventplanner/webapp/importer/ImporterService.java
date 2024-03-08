package org.eventplanner.webapp.importer;

import org.eventplanner.webapp.config.Permission;
import org.eventplanner.webapp.config.SignedInUser;
import org.eventplanner.webapp.events.EventRepository;
import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.users.UserRepository;
import org.eventplanner.webapp.users.models.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class ImporterService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ImporterService(
            @Autowired EventRepository eventRepository,
            @Autowired UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public void importEvents(@NonNull SignedInUser signedInUser, int year, InputStream stream) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENTS);
        var users =  userRepository.findAll();
        var events = EventExcelImporter.readFromInputStream(stream, year, users);
        eventRepository.deleteAllByYear(year);
        for (Event event : events) {
            eventRepository.create(event);
        }
    }

    public void importUsers(@NonNull SignedInUser signedInUser, InputStream stream) {
        signedInUser.assertHasPermission(Permission.WRITE_USERS);
        var users = UserExcelImporter.readFromInputStream(stream);
        userRepository.deleteAll();
        for (UserDetails user : users) {
            userRepository.create(user);
        }
    }
}
