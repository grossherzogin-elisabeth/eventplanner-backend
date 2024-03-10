package org.eventplanner.webapp.config;

import java.util.Arrays;
import java.util.Optional;

public enum Role {

    NONE("ROLE_NONE"),
    ADMIN("ROLE_ADMIN"),
    EVENT_PLANNER("ROLE_EVENT_PLANNER"),
    TEAM_PLANNER("ROLE_TEAM_PLANNER"),
    TEAM_MEMBER("ROLE_TEAM_MEMBER"),
    USER_MANAGER("ROLE_USER_MANAGER"),
    TECHNICAL_USER("ROLE_TECHNICAL_USER");

    private String value;

    Role(String value)  {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Optional<Role> fromString(String value) {
        return Arrays.stream(Role.values())
                .filter(role -> role.value().equals(value))
                .findFirst();
    }
}
