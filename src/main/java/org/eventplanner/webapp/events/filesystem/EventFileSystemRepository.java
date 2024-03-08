package org.eventplanner.webapp.events.filesystem;

import org.eventplanner.webapp.events.EventRepository;
import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.utils.FileSystemRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Repository
public class EventFileSystemRepository extends FileSystemRepository<Event, EventJsonEntity> implements EventRepository {

    private static final String directory = "/tmp/eventplanner/data/events";

    public EventFileSystemRepository() {
        super(EventJsonEntity.class, new File(directory));
    }

    @Override
    public @NonNull List<Event> findAllByYear(int year) {
        var dir = new File(directory + "/" + year);
        return readAllFromDirectory(dir);
    }

    @Override
    public @NonNull Event create(@NonNull Event event) {
        var year = event.start().atZone(ZoneId.of("Europe/Berlin")).getYear();
        var key =  UUID.randomUUID().toString();
        var file = new File(directory + "/" + year + "/" + key  + ".json");
        writeToFile(file, event);
        return event;
    }

    @Override
    public @NonNull Event update(@NonNull Event event) {
        var year = event.start().atZone(ZoneId.of("Europe/Berlin")).getYear();
        var key =  UUID.randomUUID().toString();
        var file = new File(directory + "/" + year + "/" + key  + ".json");
        writeToFile(file, event);
        return event;
    }

    @Override
    public void deleteAllByYear(int year) {
        var dir = new File(directory + "/"+year);
        deleteAllInDirectory(dir);
    }

    @Override
    public String getKey(Event domain) {
        return domain.key().value();
    }

    @Override
    public EventJsonEntity mapToEntity(Event domain) {
        return EventJsonEntity.fromDomain(domain);
    }

    @Override
    public Event mapToDomain(EventJsonEntity entity) {
        return entity.toDomain();
    }
}
