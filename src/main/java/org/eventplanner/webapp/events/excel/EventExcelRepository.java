package org.eventplanner.webapp.events.excel;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.webapp.events.EventRepository;
import org.eventplanner.webapp.events.models.Event;
import org.eventplanner.webapp.positions.models.Position;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.users.models.UserKey;
import org.eventplanner.webapp.utils.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EventExcelRepository implements EventRepository {

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

            Map<UserKey, PositionKey> waitinglist = new HashMap<>();
            for (int r = 4; r < raw.length; r++) {
                var name = raw[r];
                if (!name.isBlank()) {
                    var position = mapPosition(data[0][r]);
                    waitinglist.put(UserKey.fromName(name), position);
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
        var dates = value.split("-");
        var date = dates.length > index ? dates[index] : dates[0];
        var dayMonth = date.trim().replaceAll(" ", "").split("\\.");
        String format = "yyyy-mm-ddT16:00:00.00Z";
        format = format.replace("yyyy", String.valueOf(year));
        format = format.replace("mm", dayMonth[1]);
        format = format.replace("dd", dayMonth[0]);
        return Instant.parse(format);
    }

    private PositionKey mapPosition(String value) {
        return switch (value) {
            case "Kapitän" -> new PositionKey("kapitaen");
            case "Steuermann" -> new PositionKey("steuermann");
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
            default -> throw new IllegalArgumentException("Unknown position");
        };
    }
}
