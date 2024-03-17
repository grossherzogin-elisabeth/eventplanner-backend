package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.events.models.Event;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public record EventRepresentation(
        @NonNull String key,
        @NonNull String name,
        @NonNull String state,
        @NonNull String note,
        @NonNull String description,
        @NonNull String start,
        @NonNull String end,
        @NonNull List<LocationRepresentation> locations,
        @NonNull List<SlotRepresentation> slots,
        @NonNull List<RegistrationRepresentation> registrations
) implements Serializable {

    public static EventRepresentation fromDomain(@NonNull Event event) {
        return new EventRepresentation(
                event.key().value(),
                event.name(),
                event.state().value(),
                event.note(),
                event.description(),
                event.start().toString(),
                event.end().toString(),
                event.locations().stream().map(LocationRepresentation::fromDomain).toList(),
                event.slots().stream().map(SlotRepresentation::fromDomain).toList(),
                event.registrations().stream().map(RegistrationRepresentation::fromDomain).toList());
    }
}
