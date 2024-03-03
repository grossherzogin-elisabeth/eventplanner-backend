package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.events.models.EventSlot;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public record UpdateEventTeamRequest(
        @NonNull List<EventSlotRepresentation> slots
) implements Serializable {
    public List<EventSlot> toDomain() {
        return slots.stream().map(EventSlotRepresentation::toDomain).toList();
    }
}
