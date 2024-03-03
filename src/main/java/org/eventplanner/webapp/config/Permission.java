package org.eventplanner.webapp.config;

import java.util.Arrays;
import java.util.Optional;

public enum Permission {
    READ_EVENTS("events:read"),
    WRITE_EVENTS("events:write"),
    JOIN_LEAVE_EVENT_TEAM("event-team:write-self"),
    WRITE_EVENT_TEAM("event-team:write"),
    READ_USERS("users:read"),
    READ_OWN_USER("user-details:read-self"),
    WRITE_OWN_USER("user-details:write-self"),
    READ_USER_DETAILS("user-details:read"),
    WRITE_USERS("users-details:write"),
    READ_POSITIONS("positions:read"),
    WRITE_POSITIONS("positions:write");

    private String value;

    Permission(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Optional<Permission> fromString(String value) {
        return Arrays.stream(Permission.values())
                .filter(permission -> permission.value().equals(value))
                .findFirst();
    }
}
