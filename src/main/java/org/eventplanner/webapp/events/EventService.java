package org.eventplanner.webapp.events;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.webapp.events.models.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(@Autowired EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public @NonNull List<Event> getEvents(int year) {
        var currentYear = Instant.now().atZone(ZoneId.of("Europe/Berlin")).getYear();
        if (year < currentYear - 10 || year > currentYear + 10) {
            throw new IllegalArgumentException("Invalid year");
        }
        return this.eventRepository.findAllByYear(year);
    }

    public @NonNull List<Event> importFromExcel(int year, @NonNull InputStream stream) {
        var events = new ArrayList<Event>();
        try {
            var cells = readExcelFile(stream);
            // TODO actual positions
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
                        "key",
                        raw[1],
                        "default",
                        "planned",
                        raw[3],
                        "",
                        parseStartDate(raw[2], year),
                        parseEndDate(raw[2], year),
                        waitinglist
                );
                events.add(event);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return events;
    }

    private Instant parseStartDate(String value, int year) {
        try {
            double daysSince1970 = Double.parseDouble(value);
            long millis = (long) daysSince1970 * 24 * 60 * 60 * 1000;
            return new Date(millis).toInstant();
        } catch (Exception e) {
            // expected
        }
        var dates = value.split("-");
        var dayMonth = dates[0].trim().replaceAll(" ", "").split("\\.");
        String format = "yyyy-mm-ddT16:00:00.00Z";
        format = format.replace("yyyy", String.valueOf(year));
        format = format.replace("mm", dayMonth[1]);
        format = format.replace("dd", dayMonth[0]);
        return Instant.parse(format);
    }

    private Instant parseEndDate(String value, int year) {
        try {
            double daysSince1970 = Double.parseDouble(value);
            long millis = (long) daysSince1970 * 24 * 60 * 60 * 1000;
            return new Date(millis).toInstant();
        } catch (Exception e) {
            // expected
        }
        var dates = value.split("-");
        var date = dates.length > 1 ? dates[1] : dates[0];
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
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
