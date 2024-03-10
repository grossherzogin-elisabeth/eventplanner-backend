package org.eventplanner.webapp.events.filesystem;

import org.eventplanner.webapp.events.EventRepository;
import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.positions.filesystem.PositionJsonEntity;
import org.eventplanner.webapp.utils.FileSystemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;

@Repository
public class EventFileSystemRepository implements EventRepository {

    private final File directory;
    private final FileSystemRepository<EventJsonEntity> fs;

    public EventFileSystemRepository(@Value("${custom.data-directory}") String dataDirectory) {
        directory = new File(dataDirectory + "/events");
        this.fs = new FileSystemRepository<>(EventJsonEntity.class, directory);
    }

    @Override
    public @NonNull List<Event> findAllByYear(int year) {
        var dir = new File(directory.getPath() + "/" + year);
        return fs.readAllFromDirectory(dir).stream()
                .map(EventJsonEntity::toDomain)
                .sorted(comparing(Event::start))
                .toList();
    }

    @Override
    public @NonNull Event create(@NonNull Event event) {
        var year = event.start().atZone(ZoneId.of("Europe/Berlin")).getYear();
        var key =  UUID.randomUUID().toString();
        var file = new File(directory.getPath() + "/" + year + "/" + key  + ".json");
        fs.writeToFile(file, EventJsonEntity.fromDomain(event));
        return event;
    }

    @Override
    public @NonNull Event update(@NonNull Event event) {
        var year = event.start().atZone(ZoneId.of("Europe/Berlin")).getYear();
        var key =  UUID.randomUUID().toString();
        var file = new File(directory.getPath() + "/" + year + "/" + key  + ".json");
        fs.writeToFile(file, EventJsonEntity.fromDomain(event));
        return event;
    }

    @Override
    public void deleteAllByYear(int year) {
        var dir = new File(directory.getPath() + "/"+year);
        fs.deleteAllInDirectory(dir);
    }
}
