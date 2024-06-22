package org.eventplanner.webapp.events.rest.models;

import org.eventplanner.webapp.events.models.UpdateEventSpec;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import static org.eventplanner.webapp.utils.ObjectUtils.mapNullable;

public record UpdateEventRequest(
    @Nullable String name,
    @Nullable String state,
    @Nullable String note,
    @Nullable String description,
    @Nullable String start,
    @Nullable String end,
    @Nullable List<LocationRepresentation> locations,
    @Nullable List<SlotRepresentation> slots
) implements Serializable {
    public UpdateEventSpec toDomain() {
        return new UpdateEventSpec(
            name,
            state,
            note,
            description,
            mapNullable(start, ZonedDateTime::parse),
            mapNullable(end, ZonedDateTime::parse),
            mapNullable(locations, LocationRepresentation::toDomain),
            mapNullable(slots, SlotRepresentation::toDomain)
        );
    }
}
