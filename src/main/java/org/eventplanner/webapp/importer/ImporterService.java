package org.eventplanner.webapp.importer;

import org.eventplanner.webapp.config.Permission;
import org.eventplanner.webapp.config.SignedInUser;
import org.eventplanner.webapp.events.EventRepository;
import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.importer.models.ImportError;
import org.eventplanner.webapp.users.UserRepository;
import org.eventplanner.webapp.users.models.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImporterService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ImporterService(
            @Autowired EventRepository eventRepository,
            @Autowired UserRepository userRepository,
            @Value("${custom.data-directory}") String dataDirectory
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;

        var users = new File(dataDirectory + "/import/users.xlsx");
        if (users.exists()) {
            log.info("Importing users from xlsx");
            try (var in = new FileInputStream(users)) {
                importUsers(SignedInUser.technicalUser(Permission.WRITE_USERS), in);
            } catch (Exception e) {
                log.error("Failed to import users on startup", e);
            }
        }

//        var events2023 = new File(dataDirectory + "/import/events-2023.xlsx");
//        if (events2023.exists()) {
//            log.info("Importing events 2023 from xlsx");
//            try (var in = new FileInputStream(events2023)) {
//                importEvents(SignedInUser.technicalUser(Permission.WRITE_EVENTS), 2023, in);
//            } catch (Exception e) {
//                log.error("Failed to import events on startup", e);
//            }
//        }

//        var events2024 = new File(dataDirectory + "/import/events-2024.xlsx");
//        if (events2024.exists()) {
//            log.info("Importing events 2024 from xlsx");
//            try (var in = new FileInputStream(events2024)) {
//                importEvents(SignedInUser.technicalUser(Permission.WRITE_EVENTS), 2024, in);
//            } catch (Exception e) {
//                log.error("Failed to import events on startup", e);
//            }
//        }
    }

    public List<ImportError> importEvents(@NonNull SignedInUser signedInUser, int year, InputStream stream) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENTS);
        var users =  userRepository.findAll();
        var errors = new ArrayList<ImportError>();
        var events = EventExcelImporter.readFromInputStream(stream, year, users, errors);
        eventRepository.deleteAllByYear(year);
        for (Event event : events) {
            eventRepository.create(event);
        }
        return errors;
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
