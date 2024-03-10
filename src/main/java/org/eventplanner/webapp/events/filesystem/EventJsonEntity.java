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

import static org.eventplanner.webapp.utils.ObjectUtils.mapNullable;

public record EventJsonEntity(
        @NonNull String key,
        @NonNull String name,
        @Nullable String state,
        @Nullable String note,
        @Nullable String description,
        @NonNull String start,
        @NonNull String end,
        @Nullable List<LocationJsonEntity> locations,
        @Nullable List<SlotJsonEntity> slots,
        @Nullable List<RegistrationJsonEntity> registrations
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
                domain.locations().stream().map(LocationJsonEntity::fromDomain).toList(),
                domain.slots().stream().map(SlotJsonEntity::fromDomain).toList(),
                domain.registrations().stream().map(RegistrationJsonEntity::fromDomain).toList());
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
                mapNullable(locations, LocationJsonEntity::toDomain, Collections.emptyList()),
                mapNullable(slots, SlotJsonEntity::toDomain, Collections.emptyList()),
                mapNullable(registrations, RegistrationJsonEntity::toDomain, Collections.emptyList()));
    }
}
