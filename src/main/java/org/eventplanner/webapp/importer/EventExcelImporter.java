package org.eventplanner.webapp.importer;

import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.events.models.EventKey;
import org.eventplanner.webapp.events.models.EventLocation;
import org.eventplanner.webapp.events.models.EventSlot;
import org.eventplanner.webapp.events.models.EventState;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserDetails;
import org.eventplanner.webapp.users.models.UserKey;
import org.eventplanner.webapp.utils.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class EventExcelImporter {

    private static final Logger log = LoggerFactory.getLogger(EventExcelImporter.class);

    public static @NonNull List<Event> readFromInputStream(@NonNull InputStream in, int year, List<UserDetails> knownUsers) {
        try {
            var data = ExcelUtils.readExcelFile(in);
            return parseEvents(data, year, knownUsers);
        } catch (Exception e) {
            log.error("Failed to read excel file", e);
        }
        return Collections.emptyList();
    }

    private static List<Event> parseEvents(String[][] data, int year, List<UserDetails> knownUsers) {
        var events = new ArrayList<Event>();
        for (int i = 1; i < data.length; i++) {
            var raw = data[i];

            var slots = generateDefaultEventSlots();
            var waitingListReached = false;
            var waitinglist = new HashMap<UserKey, PositionKey>();
            for (int r = 4; r < raw.length; r++) {
                var name = raw[r];
                if (name.isBlank()
                        || name.equals("noch zu benennen")
                        || name.equals("noch zu besetzen")) {
                    continue;
                }
                if (name.contains("Warteliste")) {
                    waitingListReached = true;
                    continue;
                }
                var userKey = findMatchingUser(name, knownUsers)
                        .map(UserDetails::key)
                        .orElse(null);
                var positionKey = mapPosition(data[0][r]);
                if (waitingListReached) {
                    if (userKey != null) {
                        waitinglist.put(userKey, positionKey);
                    } else {
                        waitinglist.put(new UserKey(name), positionKey);
                    }
                } else {
                    try {
                        slots = fillFirstMatchingSlot(slots, userKey, name, positionKey);
                    } catch (Exception e) {
                        // log.warn("Failed to find matching slot for user "+name+", adding user to waiting list");
                        waitinglist.put(userKey, positionKey);
                    }
                }
            }
            var event = new Event(
                    new EventKey(String.valueOf(i)),
                    raw[1],
                    EventState.PLANNED,
                    raw[3],
                    "",
                    parseExcelDate(raw[2], year, 0),
                    parseExcelDate(raw[2], year, 1),
                    getLocationsFromText(raw[1]),
                    slots,
                    waitinglist
            );
            events.add(event);
        }
        return events;
    }

    private static Optional<UserDetails> findMatchingUser(String name, List<UserDetails> allUsers) {
        var normalizedName = name
                .trim()
                .replace("mit Ü", "") // used as flag
                .replace("u. V.", "") // used as flag
                .replace("u.V.", "") // used as flag
                .replace("?", "") // used as flag
                .replace(" fix", "") // used as flag
//                .replaceAll("\\s", " ") // remove whitespace characters
//                .replaceAll("  ", " ") // remove duplicate whitespace characters
                .replaceAll("\\(.*\\)", "") // remove everything in brackets e.g. (this)
                .replaceAll("[^a-zA-ZöäüÖÄÜß\\-. ]", ""); // keep only a-z characters and a few symbols
        final var nameParts = normalizedName.split(" ");
        nameParts[0] = resolveAbbreviations(nameParts[0]);
        nameParts[1] = resolveAbbreviations(nameParts[1]);
        if (nameParts.length > 2) {
            if (name.contains(",")) {
                nameParts[1] = nameParts[1] + " " + nameParts[2];
            } else {
                nameParts[0] = nameParts[0] + " " + nameParts[1];
                nameParts[1] = nameParts[2];
            }
        }
        // search for exact match
        var exactMatch = allUsers.stream()
                .filter(user -> (user.lastName().equalsIgnoreCase(nameParts[0]) && user.firstName().equalsIgnoreCase(nameParts[1]))
                        || (user.lastName().equalsIgnoreCase(nameParts[1]) && user.firstName().equalsIgnoreCase(nameParts[0])))
                .findFirst();
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        // search for exact match in last name and starts with in first name
        var lastNameMatch = allUsers.stream()
                .filter(user -> (user.lastName().equalsIgnoreCase(nameParts[0]) && user.firstName().startsWith(nameParts[1]))
                        || (user.lastName().equalsIgnoreCase(nameParts[1]) && user.firstName().startsWith(nameParts[0])))
                .findFirst();
        if (lastNameMatch.isPresent()) {
            var user = lastNameMatch.get();
            log.info("Found last name match for name " + name + " on " + user.firstName() + " " + user.lastName());
            return lastNameMatch;
        }
        log.warn("Could not find a matching user for " + name);
        return Optional.empty();
        // Kullmann, Philipp Masch -> Kaderliste ohne "Masch"
        // Fischer, Alina -> Kaderliste "Ficher"
        // Buss, Nicole -> Kaderliste "Buss"
        // Schwolow, Pia Marie -> Kaderliste "Pia-Marie"
        // Jänke, Till + Finn -> zwei Namen
        // Manhardt, Rudi -> Kaderliste "Rudolf"
        // N.N. neu (KW) -> was?
        // Melchior, Christine ab 06.06. -> (ab 06.06.) bitte in Klammern
        // Spierling, Úwe -> Kaderliste "Uwe"
        // Sichau, Jutta KOJE
    }

    private static @NonNull String resolveAbbreviations(@NonNull String in) {
        if (in.equals("HaWe")) {
            return "Hans-Werner";
        }
        if (in.equals("H.U.")) {
            return "Hans-Ulrich";
        }
        if (in.equals("Rudi")) {
            return "Rudolf";
        }
        return in;
    }

    private static Instant parseExcelDate(String value, int year, int index) {
        try {
            var instant = Instant.parse(value);
            if (instant.atZone(ZoneId.of("Europe/Berlin")).getYear() == year) {
                return instant;
            }
        } catch (DateTimeParseException e) {
            // expected
        } catch (Exception e) {
            // unexpected, but the fallback will probably get the right date
            log.warn("Unexpected error during date conversion", e);
        }
        var dates = value
                .replaceAll("\\s", "") // remove whitespace characters
                .replaceAll("[^0-9.-]", "") // remove all non a-z characters
                .split("-");
        var date = dates.length > index ? dates[index] : dates[0];
        var dayMonth = Arrays.stream(date.split("\\.")).filter(it -> !it.isBlank()).toList();
        String format = "yyyy-mm-ddT16:00:00.00Z";
        format = format.replace("yyyy", String.valueOf(year));
        format = format.replace("mm", dayMonth.get(1));
        format = format.replace("dd", dayMonth.get(0));
        return Instant.parse(format);
    }

    private static PositionKey mapPosition(String value) {
        return switch (value) {
            case "Kapitän" -> DefaultPositions.POSITION_KAPITAEN;
            case "Stm.", "Steuermann" -> DefaultPositions.POSITION_STM;
            case "NOA" -> DefaultPositions.POSITION_NOA;
            case "1. Maschinist", "2. Maschinist", "3. Maschinist (Ausb.)" -> DefaultPositions.POSITION_MASCHINIST;
            case "Koch" -> DefaultPositions.POSITION_KOCH;
            case "Ausbilder" -> DefaultPositions.POSITION_AUSBILDER;
            case "Matrose" -> DefaultPositions.POSITION_MATROSE;
            case "Leichtmatrose" -> DefaultPositions.POSITION_LEICHTMATROSE;
            case "Decksmann / -frau" -> DefaultPositions.POSITION_DECKSHAND;
            case "Backschaft" -> DefaultPositions.POSITION_BACKSCHAFT;
            default -> throw new IllegalArgumentException("Unknown position");
        };
    }

    private static @NonNull List<EventSlot> fillFirstMatchingSlot(
            @NonNull List<EventSlot> slots,
            @Nullable UserKey userKey,
            @Nullable String name,
            @NonNull PositionKey position
    ) {
        for (int i = 0; i < slots.size(); i++) {
            var slot = slots.get(i);
            if (slot.assignedUser() == null && slot.positions().contains(position)) {
                if (userKey != null) {
                    slots.set(i, slot.withAssignedUser(userKey, position));
                } else {
                    slots.set(i, slot.withAssignedPerson(name, position));
                }
                return slots;
            }
        }
        throw new IllegalArgumentException("No matching slot found");
    }

    private static List<EventLocation> getLocationsFromText(String text) {
        var elsfleth = new EventLocation("Elsfleth", "fa-anchor", "An d. Kaje 1, 26931 Elsfleth", "DE");
        var bremerhaven = new EventLocation("Bremerhaven", "fa-anchor", null, "DE");
        var rosstock = new EventLocation("Rosstock", "fa-anchor", null, "DE");
        var mariehamn = new EventLocation("Mariehamn", "fa-anchor", null, "FI");
        var stettin = new EventLocation("Stettin", "fa-anchor", null, "PL");
        var nok = new EventLocation("Nord-Ostsee-Kanal", "fa-water text-blue-600", null, "DE");
        var nordsee = new EventLocation("Nordsee", "fa-water text-blue-600",null,null);
        var ostsee = new EventLocation("Ostsee", "fa-water text-blue-600",null,null);
        var weser = new EventLocation("Weser", "fa-water text-blue-600",null,null);

        var textNormalized = text.replaceAll("\s", "").toLowerCase();

        if (textNormalized.contains("elsfleth-nordsee-elsfleth")) {
            return List.of(elsfleth, nordsee, elsfleth);
        }
        if (textNormalized.contains("sr1")) {
            return List.of(elsfleth, nok, mariehamn);
        }
        if (textNormalized.contains("sr2")) {
            return List.of(mariehamn, ostsee, stettin);
        }
        if (textNormalized.contains("sr3")) {
            return List.of(stettin, ostsee, rosstock);
        }
        if (textNormalized.contains("sr4")) {
            return List.of(rosstock, nok, bremerhaven);
        }
        if (textNormalized.contains("maritimetage")) {
            return List.of(bremerhaven, nordsee, bremerhaven);
        }
        if (textNormalized.contains("hansesail")) {
            return List.of(rosstock, ostsee, rosstock);
        }
        if (textNormalized.contains("tagesfahrt") || textNormalized.contains("abendfahrt")) {
            return List.of(elsfleth, weser, elsfleth);
        }
        return List.of(elsfleth, nordsee, elsfleth);
    }

    private static List<EventSlot> generateDefaultEventSlots() {
        var slots = new ArrayList<EventSlot>();
        slots.add(EventSlot.of(DefaultPositions.POSITION_KAPITAEN).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_STM, DefaultPositions.POSITION_KAPITAEN).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_STM, DefaultPositions.POSITION_KAPITAEN).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_STM, DefaultPositions.POSITION_KAPITAEN).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_NOA));
        slots.add(EventSlot.of(DefaultPositions.POSITION_MASCHINIST).withName("1. Maschinist").withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_MASCHINIST).withName("2. Maschinist").withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_MASCHINIST).withName("3. Maschinist (Ausb.)"));
        slots.add(EventSlot.of(DefaultPositions.POSITION_KOCH).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_KOCH).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_KOCH));
        slots.add(EventSlot.of(DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(EventSlot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(EventSlot.of(DefaultPositions.POSITION_BACKSCHAFT));
        slots.add(EventSlot.of(DefaultPositions.POSITION_BACKSCHAFT));
        slots.add(EventSlot.of(DefaultPositions.POSITION_BACKSCHAFT));
        return slots;
    }
}
