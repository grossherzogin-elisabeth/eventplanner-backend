package org.eventplanner.webapp.events.excel;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.webapp.events.EventRepository;
import org.eventplanner.webapp.events.models.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class EventExcelRepository implements EventRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ResourceLoader resourceLoader;

    public EventExcelRepository(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public @NonNull List<Event> findAllByYear(int year) {
        try(InputStream in = new FileInputStream("/tmp/eventplanner/data/events-"+year+".xlsx")) {
            return importFromExcel(year, in);
        } catch (Exception e) {
            log.error("Failed to read excel file", e);
            return Collections.emptyList();
        }
    }

    private List<Event> importFromExcel(int year, @NonNull InputStream stream) throws IOException {
        var events = new ArrayList<Event>();
        var cells = readExcelFile(stream);
        // TODO read planned positions
        for (int i = 1; i < cells.length; i++) {
            var raw = cells[i];

            Map<String, String> waitinglist = new HashMap<>();
            for (int r = 4; r < raw.length; r++) {
                var name = raw[r];
                if (!name.isBlank()) {
                    var position = mapPosition(cells[0][r]);
                    waitinglist.put(hashName(name), position);
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
        } catch (Exception e) {
            e.printStackTrace();
            // expected
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

    private String mapPosition(String value) {
        return switch (value) {
            case "Kapitän" -> "kapitaen";
            case "Steuermann" -> "steuermann";
            case "NOA" -> "noa";
            case "1. Maschinist" -> "maschinist";
            case "2. Maschinist" -> "maschinist";
            case "3. Maschinist (Ausb.)" -> "maschinist";
            case "Koch" -> "koch";
            case "Ausbilder" -> "ausbilder";
            case "Matrose" -> "matrose";
            case "Leichtmatrose" -> "leichtmatrose";
            case "Decksmann / -frau" -> "deckshand";
            case "Backschaft" -> "backschaft";
            default -> "";
        };
    }

    private String hashName(String value) {
        var name = value;
        if (value.contains(",")) {
            var parts = value.split(",");
            name = parts[1].trim() + " " + parts[0].trim();
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(name.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            return "err";
        }
    }

    private String[][] readExcelFile(InputStream stream) throws IOException {
        var workbook = new XSSFWorkbook(stream);
        var sheet = workbook.getSheetAt(0);

        int colCount = 5;
        while (!getCellValueAsString(sheet, 0, colCount).isBlank()) {
            colCount++;
        }

        var rowCount = 3;
        while (!getCellValueAsString(sheet, rowCount, 0).isBlank()) {
            rowCount++;
        }

        String[][] cells = new String[colCount][rowCount];
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                cells[c][r] = getCellValueAsString(sheet, r, c);
            }
        }
        return cells;
    }

    private static String getCellValueAsString(XSSFSheet sheet, int r, int c) {
        var row = sheet.getRow(r);
        if (row == null) {
            return "";
        }
        var cell = row.getCell(c);
        if (cell == null) {
            return "";
        }
        try {
            var date = cell.getDateCellValue();
            return date.toInstant().toString();
        } catch (Exception e) {
            // ignore
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
