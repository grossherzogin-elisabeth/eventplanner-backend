package org.eventplanner.driving.rest.models;

import org.eventplanner.domain.importer.models.ImportError;
import org.eventplanner.domain.importer.models.ImportError;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record ImportErrorRepresentation(
    @NonNull String eventKey,
    @NonNull String eventName,
    @NonNull String start,
    @NonNull String end,
    @NonNull String message
) implements Serializable {
    public static ImportErrorRepresentation fromDomain(ImportError domain) {
        return new ImportErrorRepresentation(
            domain.eventKey().value(),
            domain.eventName(),
            domain.start().toString(),
            domain.end().toString(),
            domain.message());
    }
}
