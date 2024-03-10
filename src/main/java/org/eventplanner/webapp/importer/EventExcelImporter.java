package org.eventplanner.webapp.importer;

import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.events.models.EventKey;
import org.eventplanner.webapp.events.models.Location;
import org.eventplanner.webapp.events.models.Registration;
import org.eventplanner.webapp.events.models.Slot;
import org.eventplanner.webapp.events.models.EventState;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserDetails;
import org.eventplanner.webapp.utils.ExcelUtils;
import org.eventplanner.webapp.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

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

import static org.eventplanner.webapp.utils.ObjectUtils.mapNullable;

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
        if (knownUsers.isEmpty()) {
            log.warn("Userlist is empty, cannot resolve any username!");
        }
        var events = new ArrayList<Event>();
        var eventErrors = new HashMap<String, List<String>>();
        for (int i = 1; i < data.length; i++) {
            var errors = new ArrayList<String>();
            var raw = data[i];
            var eventName = raw[1].trim()
                    .replaceAll("\n", " ")
                    .replaceAll("  ", "");
            var start = parseExcelDate(raw[2], year, 0);
            var end = parseExcelDate(raw[2], year, 1);

            var slots = generateDefaultEventSlots(eventName);
            var waitingListReached = false;
            var registrations = new ArrayList<Registration>();
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
                var user = findMatchingUser(name, knownUsers).orElse(null);
                if (user == null) {
                    errors.add("Failed to find user '" + name + "'");
                }
                var userKey = mapNullable(user, UserDetails::key);
                var positionKey = mapPosition(data[0][r]);
                if (user != null && !user.positions().contains(positionKey)) {
                    errors.add("User " + user.fullName() + " does not have position '" + positionKey.value() + "'! " +
                            "Changing position to '" + user.positions().getFirst().value() + "'");
                    positionKey = user.positions().getFirst();
                }
                var registration = userKey != null
                        ? Registration.ofUser(userKey, positionKey)
                        : Registration.ofPerson(name, positionKey);
                if (!waitingListReached) {
                    try {
                        registration = assignToFirstMatchingSlot(registration, slots, registrations);
                    } catch (Exception e) {
                        log.warn("Failed to find matching " + positionKey.value() + " slot for "  +  name + " at event " + eventName + " starting on " + start.toString());
                    }
                }
                registrations.add(registration);
            }
            var event = new Event(
                    new EventKey(String.valueOf(i)),
                    eventName,
                    EventState.PLANNED,
                    raw[3],
                    "",
                    start,
                    end,
                    getLocationsFromText(raw[1]),
                    slots,
                    registrations
            );
            events.add(event);
            eventErrors.put(event.name(), errors);
        }
        return events;
    }

    private static Optional<UserDetails> findMatchingUser(String name, List<UserDetails> allUsers) {
        if (allUsers.isEmpty()) {
            return Optional.empty();
        }
        try {
            var normalizedName = name
                    .trim()
                    .replace("mit Ü", "") // used as flag
                    .replace("u. V.", "") // used as flag
                    .replace("u.V.", "") // used as flag
                    .replace("?", "") // used as flag
                    .replace(" fix", "") // used as flag
                    .replace(",", " ") // there are some names without whitespace after the ','
                    .replace("  ", " ") // remove duplicate whitespaces
                    .replaceAll("\\(.*\\)", "") // remove everything in brackets e.g. (this)
                    .replaceAll("[^a-zA-ZöäüÖÄÜß\\-. ]", ""); // keep only a-z characters and a few symbols
            final var nameParts = normalizedName.split(" ");
            nameParts[0] = resolveAbbreviations(nameParts[0]).trim();
            nameParts[1] = resolveAbbreviations(nameParts[1]).trim();
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
                log.debug("Found last name match for name " + name + " on " + user.firstName() + " " + user.lastName());
                return lastNameMatch;
            }
//            log.warn("Could not find a matching user for " + name);
        } catch (Exception e) {
            log.error("Failed to find a matching user for " + name, e);
        }
        return Optional.empty();
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
        if (in.equals("K.-L.")) {
            return "Karl-Ludwig";
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

    private static @NonNull Registration assignToFirstMatchingSlot(
            @NonNull Registration registration,
            @NonNull List<Slot> slots,
            @NonNull List<Registration> registrations
    ) {
        var occupiedSlots = registrations.stream().map(Registration::slot).toList();
        var matchingSlot = slots.stream()
                .filter(slot -> !occupiedSlots.contains(slot.key()))
                .filter(slot -> slot.positions().contains(registration.position()))
                .findFirst();
        if (matchingSlot.isPresent()) {
            return registration.withSlot(matchingSlot.get().key());
        }
        throw new IllegalStateException("No matching slot found");
    }

    private static List<Location> getLocationsFromText(String text) {
        var elsfleth = new Location("Elsfleth", "fa-anchor", "An d. Kaje 1, 26931 Elsfleth", "DE");
        var bremerhaven = new Location("Bremerhaven", "fa-anchor", null, "DE");
        var rosstock = new Location("Rosstock", "fa-anchor", null, "DE");
        var mariehamn = new Location("Mariehamn", "fa-anchor", null, "FI");
        var stettin = new Location("Stettin", "fa-anchor", null, "PL");
        var nok = new Location("Nord-Ostsee-Kanal", "fa-water text-blue-600", null, "DE");
        var nordsee = new Location("Nordsee", "fa-water text-blue-600",null,null);
        var ostsee = new Location("Ostsee", "fa-water text-blue-600",null,null);
        var weser = new Location("Weser", "fa-water text-blue-600",null,null);

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

    private static List<Slot> generateDefaultEventSlots(String eventName) {
        var slots = new ArrayList<Slot>();
        slots.add(Slot.of(DefaultPositions.POSITION_KAPITAEN).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_STM, DefaultPositions.POSITION_KAPITAEN).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_STM, DefaultPositions.POSITION_KAPITAEN).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_STM, DefaultPositions.POSITION_KAPITAEN).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_STM, DefaultPositions.POSITION_KAPITAEN));
        slots.add(Slot.of(DefaultPositions.POSITION_NOA));
        slots.add(Slot.of(DefaultPositions.POSITION_MASCHINIST).withName("1. Maschinist").withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_MASCHINIST).withName("2. Maschinist").withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_MASCHINIST).withName("Maschinist (Ausb.)"));
        slots.add(Slot.of(DefaultPositions.POSITION_MASCHINIST).withName("Maschinist (Ausb.)"));
        slots.add(Slot.of(DefaultPositions.POSITION_KOCH).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_KOCH).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_KOCH));
        slots.add(Slot.of(DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER).withRequired());
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
        if (eventName.equals("Ausbildungsfahrt Crew")) {
            for (int i = 0; i < 30; i++) {
                slots.add(Slot.of(DefaultPositions.POSITION_DECKSHAND, DefaultPositions.POSITION_MATROSE, DefaultPositions.POSITION_LEICHTMATROSE, DefaultPositions.POSITION_AUSBILDER));
            }
        }
        slots.add(Slot.of(DefaultPositions.POSITION_BACKSCHAFT));
        slots.add(Slot.of(DefaultPositions.POSITION_BACKSCHAFT));
        slots.add(Slot.of(DefaultPositions.POSITION_BACKSCHAFT));

        for (int i = 0; i < slots.size(); i++) {
            slots.set(i, slots.get(i).withOrder(i + 1));
        }

        return slots;
    }
}
