package org.eventplanner.webapp.users.excel;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.positions.models.Position;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.UserRepository;
import org.eventplanner.webapp.users.models.AuthKey;
import org.eventplanner.webapp.users.models.User;
import org.eventplanner.webapp.users.models.UserKey;
import org.eventplanner.webapp.utils.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public class UserExcelRepository implements UserRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public @NonNull List<User> findAll() {
        try {
            var dir = new File("/tmp/eventplanner/data");
            var files = dir.listFiles();
            if (files == null) {
                return Collections.emptyList();
            }
            var users = new HashMap<UserKey, User>();
            for (File file : files) {
                var data = ExcelUtils.readExcelFile(file);
                extractUsers(data).forEach((key, user) -> {
                    if (users.containsKey(key)) {
                        users.put(key, mergeUserPositions(users.get(key), user.positions()));
                    } else {
                        users.put(key, user);
                    }
                });
            }
            return users.values().stream().toList();
        } catch (Exception e) {
            log.error("Failed to read excel file", e);
        }
        return Collections.emptyList();
    }

    @Override
    public @NonNull Optional<User> findByKey(@NonNull UserKey key) {
        return findAll().stream()
                .filter(it -> key.equals(it.key()))
                .findFirst();
    }

    @Override
    public @NonNull Optional<User> findByAuthKey(@NonNull AuthKey key) {
        return findAll().stream()
                .filter(it -> key.equals(it.authKey()))
                .findFirst();
    }

    private @NonNull Map<UserKey, User> extractUsers(@NonNull String[][] data) {
        var users = new HashMap<UserKey, User>();
        for (int i = 1; i < data.length; i++) {
            var col = data[i];
            for (int r = 4; r < col.length; r++) {
                var raw = col[r].trim();
                if (raw != null && !raw.isBlank() && !raw.equals("noch zu besetzen") && !raw.contains("Warteliste")) {
                    var key = UserKey.fromName(raw);
                    var name = normalizeName(raw);
                    try {
                        var position = mapPosition(data[0][r]);
                        var user = users.getOrDefault(key, new User(
                                key,
                                null,
                                name[0],
                                name[1],
                                name[0]+"."+name[1]+"@email.de",
                                Collections.singletonList(position),
                                List.of(Role.TEAM_MEMBER)
                        ));
                        users.put(key, mergeUserPositions(user, Collections.singletonList(position)));
                    } catch (Exception e) {
                        log.error("Failed to map user " + raw, e);
                    }
                }
            }
        }
        return users;
    }

    private static @NonNull User mergeUserPositions(@NonNull User user, @NonNull List<PositionKey> positions) {
        return new User(
                user.key(),
                user.authKey(),
                user.firstName(),
                user.lastName(),
                user.email(),
                Stream.concat(user.positions().stream(), positions.stream())
                        .distinct()
                        .toList(),
                user.roles()
        );
    }

    private static @NonNull String[] normalizeName(@NonNull String raw) {
        var name = raw
                .replace("mit Ü", "")
                .replace("u. V.", "")
                .replace("?", "")
                .replaceAll("\\(.*\\)", "") // remove everything in brackets e.g. (this)
                .replaceAll("[^a-zA-ZöäüÖÄÜß., ]", "") // remove all non a-z
                .trim();

        var firstName = "";
        var lastName = "";
        if (name.contains(",")) {
            var parts = name.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid name with more than one ','");
            }
            firstName = parts[1].trim();
            lastName = parts[0].trim();
        } else {
            var parts = name.split(" ");
            firstName = parts[0].trim();
            parts[0] = "";
            lastName = String.join(" ", parts).trim();
        }

        if (firstName == "H.U.") {
            firstName = "Hans-Ulrich";
        }

        return new String[]{firstName, lastName};
    }

    private static @NonNull PositionKey mapPosition(@NonNull String value) {
        return switch (value) {
            case "Kapitän" -> new PositionKey("kapitaen");
            case "Steuermann" -> new PositionKey("steuermann");
            case "Stm." -> new PositionKey("steuermann");
            case "NOA" -> new PositionKey("noa");
            case "1. Maschinist" -> new PositionKey("maschinist");
            case "2. Maschinist" -> new PositionKey("maschinist");
            case "3. Maschinist (Ausb.)" -> new PositionKey("maschinist");
            case "Koch" -> new PositionKey("koch");
            case "Ausbilder" -> new PositionKey("ausbilder");
            case "Matrose" -> new PositionKey("matrose");
            case "Leichtmatrose" -> new PositionKey("leichtmatrose");
            case "Decksmann / -frau" -> new PositionKey("deckshand");
            case "Backschaft" -> new PositionKey("backschaft");
            default -> throw new IllegalArgumentException("Unknown position: " + value);
        };
    }
}
