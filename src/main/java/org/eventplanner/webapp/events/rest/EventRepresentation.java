package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.User;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public record EventRepresentation(
        @NonNull String key,
        @NonNull String name,
        @NonNull String note,
        @NonNull String description,
        @NonNull String start,
        @NonNull String end,
        @NonNull Map<String, String> waitingList
) implements Serializable {

    public static EventRepresentation fromDomain(@NonNull Event event) {
        return new EventRepresentation(
                event.key(),
                event.name(),
                event.note(),
                event.description(),
                event.start().toString(),
                event.end().toString(),
                mapWaitingList(event.waitingList())
        );
    }

    private static Map<String, String> mapWaitingList(Map<UserKey, PositionKey> in) {
        var out = new HashMap<String, String>();
        in.forEach((key1, value) -> out.put(key1.value(), value.value()));
        return out;
    }
}
