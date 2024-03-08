package org.eventplanner.webapp.events.filesystem;

import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.events.models.EventKey;
import org.eventplanner.webapp.events.models.EventState;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record EventJsonEntity(
        @NonNull String key,
        @NonNull String name,
        @Nullable String state,
        @Nullable String note,
        @Nullable String description,
        @NonNull String start,
        @NonNull String end,
        @Nullable List<EventLocationJsonEntity> locations,
        @Nullable List<EventSlotJsonEntity> slots,
        @Nullable Map<String, String> waitingList
) implements Serializable {
    public static @NonNull EventJsonEntity fromDomain(@NonNull Event domain) {
        return new EventJsonEntity(
                domain.key().value(),
                domain.name(),
                domain.state().value(),
                domain.note(),
                domain.description(),
                domain.start().toString(),
                domain.end().toString(),
                domain.locations().stream().map(EventLocationJsonEntity::fromDomain).toList(),
                domain.slots().stream().map(EventSlotJsonEntity::fromDomain).toList(),
                mapWaitingListToEntity(domain.waitingList())
        );
    }

    public Event toDomain() {
        return new Event(
                new EventKey(key),
                name,
                EventState.fromString(state).orElse(EventState.PLANNED),
                note != null ? note : "",
                description != null ? description : "",
                Instant.parse(start),
                Instant.parse(end),
                locations != null
                        ? locations.stream().map(EventLocationJsonEntity::toDomain).toList()
                        : Collections.emptyList(),
                slots != null
                        ? slots.stream().map(EventSlotJsonEntity::toDomain).toList()
                        : Collections.emptyList(),
                waitingList != null
                        ? mapWaitingListToDomain(waitingList)
                        : Collections.emptyMap());
    }

    private static @NonNull Map<UserKey, PositionKey> mapWaitingListToDomain(@NonNull Map<String, String> in) {
        var out = new HashMap<UserKey, PositionKey>();
        in.forEach((key, value) -> out.put(new UserKey(key), new PositionKey(value)));
        return out;
    }

    private static @NonNull Map<String, String> mapWaitingListToEntity(@NonNull Map<UserKey, PositionKey> in) {
        var out = new HashMap<String, String>();
        in.forEach((key, value) -> out.put(key.value(), value.value()));
        return out;
    }
}
