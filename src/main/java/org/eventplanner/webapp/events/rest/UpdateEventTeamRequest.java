package org.eventplanner.webapp.events.rest;

import org.eventplanner.webapp.events.models.Registration;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public record UpdateEventTeamRequest(
        @NonNull List<RegistrationRepresentation> registrations
) implements Serializable {
    public List<Registration> toDomain() {
        return registrations.stream().map(RegistrationRepresentation::toDomain).toList();
    }
}
