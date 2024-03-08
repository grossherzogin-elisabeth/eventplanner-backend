package org.eventplanner.webapp.importer;

import org.eventplanner.webapp.config.Role;
import org.eventplanner.webapp.positions.models.PositionKey;
import org.eventplanner.webapp.qualifications.models.QualificationKey;
import org.eventplanner.webapp.users.models.Address;
import org.eventplanner.webapp.users.models.UserDetails;
import org.eventplanner.webapp.users.models.UserKey;
import org.eventplanner.webapp.users.models.UserQualification;
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

public class UserExcelImporter {

    private static final int COL_LASTNAME = 1;
    private static final int COL_FIRSTNAME = 2;
    private static final int COL_STREET = 3;
    private static final int COL_ZIPCODE = 4;
    private static final int COL_TOWN = 5;
    private static final int COL_EMAIL = 6;
    private static final int COL_MOBILE = 7;
    private static final int COL_PHONE_OFFICE = 8;
    private static final int COL_PHONE_PRIVATE = 9;
    private static final int COL_FAX = 10;
    private static final int COL_DATE_OF_BIRTH = 11;
    private static final int COL_TOWN_OF_BIRTH = 12;
    private static final int COL_PASS_NR = 13;
    private static final int COL_POSITION = 14;
    private static final int COL_NATIONALITY = 15;
    private static final int COL_MEMBER = 16;
    private static final int COL_RIGG_SUITABLE = 17;
    private static final int COL_FITNESS_FOR_SEA_SERVICE_EXPIRATION_DATE = 18;
    private static final int COL_QUALIFICATION = 19;
    private static final int COL_QUALIFICATION_EXPIRATION_DATE = 20;
    private static final int COL_FUNK = 21;
    private static final int COL_FUNK_EXPIRATION_DATE = 22;
    private static final int COL_STCW = 23;
    private static final int COL_STCW_EXPIRATION_DATE = 24;
    private static final int COL_MEDICAL_CARE = 25;
    private static final int COL_FIRST_AID = 25;
    private static final int COL_OTHER_QUALIFICATIONS = 26;

    private static final Logger log = LoggerFactory.getLogger(UserExcelImporter.class);

    public static @NonNull List<UserDetails> readFromInputStream(@NonNull InputStream in) {
        try {
            var data = ExcelUtils.readExcelFile(in);
            return parseUsers(data);
        } catch (Exception e) {
            log.error("Failed to read excel file", e);
        }
        return Collections.emptyList();
    }

    private static @NonNull List<UserDetails> parseUsers(@NonNull String[][] data) {
        var users = new HashMap<UserKey, UserDetails>();
        for (int r = 1; r < data[0].length; r++) {
            var lastName = data[COL_LASTNAME][r].trim();
            var firstName = data[COL_FIRSTNAME][r].trim();
            var key = UserKey.fromName(firstName + " " + lastName);
            var position = mapPosition(data[COL_POSITION][r]);
            UserDetails user = users.get(key);
            if (user == null) {
                var street = data[COL_STREET][r].trim();;
                var zipcode = data[COL_ZIPCODE][r].trim();;
                var town = data[COL_TOWN][r].trim();;
                Address address = null;
                if (!street.isBlank() && !zipcode.isBlank() && !town.isBlank()) {
                    address = new Address(street, town, Integer.parseInt(zipcode));
                }
                var email = data[COL_EMAIL][r].trim();
                var mobile = data[COL_MOBILE][r].trim();;
                var phone = data[COL_PHONE_PRIVATE][r].trim();;
                var dateOfBirth = data[COL_DATE_OF_BIRTH][r].trim();;
                var placeOfBirth = data[COL_TOWN_OF_BIRTH][r].trim();;
                var passNr = data[COL_PASS_NR][r].trim();;
                user = new UserDetails(
                        key,
                        null,
                        firstName,
                        lastName,
                        Collections.emptyList(),
                        List.of(Role.TEAM_MEMBER),
                        Collections.emptyList(),
                        address,
                        email.isBlank() ? null : email,
                        phone.isBlank() ? null : phone,
                        mobile.isBlank() ? null : mobile,
                        dateOfBirth.isBlank() ? null : parseExcelDate(dateOfBirth),
                        placeOfBirth.isBlank() ? null : placeOfBirth,
                        passNr.isBlank() ? null  : passNr,
                        null
                );
            }
            user = user.withAddPosition(position);
//            var fitnessForSeaService = data[COL_FITNESS_FOR_SEA_SERVICE_EXPIRATION_DATE][r].trim();;
//            if (!fitnessForSeaService.isBlank() && !fitnessForSeaService.equals("-")) {
//                user.withAddQualification(new QualificationKey())
//            }
            users.put(key, user);
        }
        return users.values().stream().toList();
    }

    private static @Nullable Instant parseExcelDate(@NonNull String value) {
        return Instant.now();
    }

    private static @NonNull PositionKey mapPosition(@NonNull String value) {
        return switch (value.toLowerCase().trim()) {
            case "cook" -> DefaultPositions.POSITION_KOCH;
            case "mate" -> DefaultPositions.POSITION_STM;
            case "master" -> DefaultPositions.POSITION_KAPITAEN;
            case "bosun" -> DefaultPositions.POSITION_AUSBILDER;
            case "motorman" -> DefaultPositions.POSITION_MASCHINIST;
            case "motormann" -> DefaultPositions.POSITION_MASCHINIST;
            case "steward" -> DefaultPositions.POSITION_BACKSCHAFT;
            case "noa" -> DefaultPositions.POSITION_NOA;
            case "lm" -> DefaultPositions.POSITION_LEICHTMATROSE;
            case "engineer" -> DefaultPositions.POSITION_MASCHINIST;
            case "ab/trainer" -> DefaultPositions.POSITION_AUSBILDER;
            case "ab" -> DefaultPositions.POSITION_MATROSE;
            case "os" -> DefaultPositions.POSITION_DECKSHAND;
            case "os (lm) ?" -> DefaultPositions.POSITION_LEICHTMATROSE;
            case "os (lm)" -> DefaultPositions.POSITION_LEICHTMATROSE;
            case "os (trainee)" -> DefaultPositions.POSITION_DECKSHAND;
            case "os (tainee)" -> DefaultPositions.POSITION_DECKSHAND;
            case "os trainee)" -> DefaultPositions.POSITION_DECKSHAND;
            case "os(trainee)" -> DefaultPositions.POSITION_DECKSHAND;
            case "child" -> DefaultPositions.POSITION_DECKSHAND;
            case "moa" -> DefaultPositions.POSITION_MASCHINIST;
            case "deckshand" -> DefaultPositions.POSITION_DECKSHAND;
            case "purser" -> DefaultPositions.POSITION_DECKSHAND; // TODO ?
            case "cadet" -> DefaultPositions.POSITION_DECKSHAND; // TODO ?
            case "" -> DefaultPositions.POSITION_DECKSHAND;
            default -> throw new IllegalArgumentException("Unknown position: " + value);
        };
    }
}
