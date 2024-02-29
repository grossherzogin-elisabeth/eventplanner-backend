package org.eventplanner.webapp.events.excel;

import org.eventplanner.webapp.events.EventRepository;
import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.events.models.EventSlot;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.eventplanner.webapp.utils.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EventExcelRepository implements EventRepository {

    private static final PositionKey POSITION_KAPITAEN = new PositionKey("kapitaen");
    private static final PositionKey POSITION_STM = new PositionKey("steuermann");
    private static final PositionKey POSITION_NOA = new PositionKey("noa");
    private static final PositionKey POSITION_MASCHINIST = new PositionKey("maschinist");
    private static final PositionKey POSITION_KOCH = new PositionKey("koch");
    private static final PositionKey POSITION_AUSBILDER = new PositionKey("ausbilder");
    private static final PositionKey POSITION_MATROSE = new PositionKey("matrose");
    private static final PositionKey POSITION_LEICHTMATROSE = new PositionKey("leichtmatrose");
    private static final PositionKey POSITION_DECKSHAND = new PositionKey("deckshand");
    private static final PositionKey POSITION_BACKSCHAFT = new PositionKey("backschaft");

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public @NonNull List<Event> findAllByYear(int year) {
        try {
            var file = new File("/tmp/eventplanner/data/events-"+year+".xlsx");
            var data = ExcelUtils.readExcelFile(file);
            return parseEvents(data, year);
        } catch (Exception e) {
            log.error("Failed to read excel file", e);
        }
        return Collections.emptyList();
    }

    private List<Event> parseEvents(String[][] data, int year) {
        var events = new ArrayList<Event>();
        for (int i = 1; i < data.length; i++) {
            var raw = data[i];

            var slots = generateDefaultEventSlots();
            var waitingListReached = false;
            var waitinglist = new HashMap<UserKey, PositionKey>();
            for (int r = 4; r < raw.length; r++) {
                var name = raw[r];
                if (name.isBlank() || name.equals("noch zu benennen")) {
                    continue;
                }
                if (name.contains("Warteliste")) {
                    waitingListReached = true;
                    continue;
                }
                var userKey = UserKey.fromName(name);
                var positionKey = mapPosition(data[0][r]);
                if (waitingListReached) {
                    waitinglist.put(userKey, positionKey);
                } else {
                    try {
                        slots = fillFirstMatchingSlot(slots, userKey, positionKey);
                    } catch (Exception e) {
                        // log.warn("Failed to find matching slot for user "+name+", adding user to waiting list");
                        waitinglist.put(userKey, positionKey);
                    }
                }
            }
            var event = new Event(
                    String.valueOf(i),
                    raw[1],
                    "default",
                    "planned",
                    raw[3],
                    "",
                    parseExcelDate(raw[2], year, 0),
                    parseExcelDate(raw[2], year, 1),
                    Collections.emptyList(),
                    slots,
                    waitinglist
            );
            events.add(event);
        }
        return events;
    }

    private Instant parseExcelDate(String value, int year, int index) {
        try {
            var instant = Instant.parse(value);
            if (instant.atZone(ZoneId.of("Europe/Berlin")).getYear() == year) {
                return instant;
            }
        } catch (DateTimeParseException e) {
            // expected
        } catch (Exception e) {
            // unexpected, but the fallback will probably get the right date
            e.printStackTrace();
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

    private PositionKey mapPosition(String value) {
        return switch (value) {
            case "Kapitän" -> POSITION_KAPITAEN;
            case "Stm.", "Steuermann" -> POSITION_STM;
            case "NOA" -> POSITION_NOA;
            case "1. Maschinist", "2. Maschinist", "3. Maschinist (Ausb.)" -> POSITION_MASCHINIST;
            case "Koch" -> POSITION_KOCH;
            case "Ausbilder" -> POSITION_AUSBILDER;
            case "Matrose" -> POSITION_MATROSE;
            case "Leichtmatrose" -> POSITION_LEICHTMATROSE;
            case "Decksmann / -frau" -> POSITION_DECKSHAND;
            case "Backschaft" -> POSITION_BACKSCHAFT;
            default -> throw new IllegalArgumentException("Unknown position");
        };
    }

    private List<EventSlot> fillFirstMatchingSlot(List<EventSlot> slots, UserKey userKey, PositionKey userPosition) {
        for (int i = 0; i < slots.size(); i++) {
            var slot = slots.get(i);
            if (slot.assignedUser() == null && slot.positions().contains(userPosition)) {
                slots.set(i, slot.withAssignedUser(userKey, userPosition));
                return slots;
            }
        }
        throw new IllegalArgumentException("No matching slot found");
    }

    private List<EventSlot> generateDefaultEventSlots() {
        var slots = new ArrayList<EventSlot>();
        slots.add(EventSlot.of(POSITION_KAPITAEN).withRequired());
        slots.add(EventSlot.of(POSITION_STM, POSITION_KAPITAEN).withRequired());
        slots.add(EventSlot.of(POSITION_STM, POSITION_KAPITAEN).withRequired());
        slots.add(EventSlot.of(POSITION_STM, POSITION_KAPITAEN).withRequired());
        slots.add(EventSlot.of(POSITION_NOA));
        slots.add(EventSlot.of(POSITION_MASCHINIST).withName("1. Maschinist").withRequired());
        slots.add(EventSlot.of(POSITION_MASCHINIST).withName("2. Maschinist").withRequired());
        slots.add(EventSlot.of(POSITION_MASCHINIST).withName("3. Maschinist (Ausb.)"));
        slots.add(EventSlot.of(POSITION_KOCH).withRequired());
        slots.add(EventSlot.of(POSITION_KOCH).withRequired());
        slots.add(EventSlot.of(POSITION_KOCH));
        slots.add(EventSlot.of(POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER).withRequired());
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER));
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER));
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER));
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER));
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER));
        slots.add(EventSlot.of(POSITION_DECKSHAND, POSITION_MATROSE, POSITION_LEICHTMATROSE, POSITION_AUSBILDER));
        slots.add(EventSlot.of(POSITION_BACKSCHAFT));
        slots.add(EventSlot.of(POSITION_BACKSCHAFT));
        slots.add(EventSlot.of(POSITION_BACKSCHAFT));
        return slots;
    }
}
