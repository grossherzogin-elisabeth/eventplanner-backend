package org.eventplanner.webapp.events;

import org.eventplanner.webapp.events.models.Event;
import org.springframework.lang.NonNull;

import java.util.List;

public interface EventRepository {
    @NonNull List<Event> findAllByYear(int year);

    @NonNull Event create(@NonNull Event event);

    void deleteAllByYear(int year);
}
