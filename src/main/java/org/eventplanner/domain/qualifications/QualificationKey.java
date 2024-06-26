package org.eventplanner.domain.qualifications;

import org.springframework.lang.NonNull;

public record QualificationKey(
    @NonNull String value
) {
}
