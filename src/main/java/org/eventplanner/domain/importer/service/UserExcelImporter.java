package org.eventplanner.domain.importer.service;

import org.eventplanner.domain.positions.models.PositionKey;
import org.eventplanner.domain.users.models.Address;
import org.eventplanner.domain.users.models.Role;
import org.eventplanner.domain.users.models.UserDetails;
import org.eventplanner.domain.users.models.UserKey;
import org.eventplanner.domain.positions.models.PositionKey;
import org.eventplanner.domain.users.models.Address;
import org.eventplanner.domain.users.models.Role;
import org.eventplanner.domain.users.models.UserDetails;
import org.eventplanner.domain.users.models.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
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

    public static @NonNull List<UserDetails> readFromFile(@NonNull File file, @Nullable String password) {
        try {
            var data = ExcelUtils.readExcelFile(file, password);
            return parseUsers(data);
        } catch (Exception e) {
            log.error("Failed to read excel file", e);
        }
        return Collections.emptyList();
    }

    private static @NonNull List<UserDetails> parseUsers(@NonNull String[][] data) {
        var users = new HashMap<UserKey, UserDetails>();
        for (int r = 1; r < data[0].length; r++) {
            var firstName = data[COL_FIRSTNAME][r].trim();
            var lastName = data[COL_LASTNAME][r].trim();
            String secondName = null;
            String title = null;
            if (firstName.contains(" ")) {
                var parts = firstName.split(" ");
                firstName = parts[0];
                secondName = "";
                for (int i = 1; i < parts.length; i++) {
                    secondName = (secondName + " " + parts[i]).trim();
                }
            }
            if (lastName.startsWith("Dr.")) {
                lastName = lastName.substring(4);
                title = "Dr.";
            }
            var key = UserKey.fromName(firstName + " " + lastName);
            var position = mapPosition(data[COL_POSITION][r]);
            UserDetails user = users.get(key);
            if (user == null) {
                var street = data[COL_STREET][r].trim();
                var zipcode = data[COL_ZIPCODE][r].trim();
                var town = data[COL_TOWN][r].trim();
                Address address = null;
                if (!street.isBlank() && !zipcode.isBlank() && !town.isBlank()) {
                    try {
                        address = new Address(street, town, (int) Double.parseDouble(zipcode));
                    } catch (NumberFormatException e) {
                        // TODO zipcodes are parsed as dates...
                        // log.warn("Failed to pare user address " + street + ", " + zipcode + " " + town);
                    }
                }
                var email = data[COL_EMAIL][r].trim().toLowerCase();
                var mobile = data[COL_MOBILE][r].trim();
                var phone = data[COL_PHONE_PRIVATE][r].trim();
                var dateOfBirth = ExcelUtils.parseExcelDate(data[COL_DATE_OF_BIRTH][r]);
                var placeOfBirth = data[COL_TOWN_OF_BIRTH][r].trim();
                var passNr = data[COL_PASS_NR][r].trim();
                user = new UserDetails(
                    key,
                    null,
                    title,
                    firstName,
                    secondName,
                    lastName,
                    Collections.emptyList(),
                    List.of(Role.TEAM_MEMBER),
                    Collections.emptyList(),
                    address,
                    email.isBlank() ? null : email,
                    phone.isBlank() ? null : phone,
                    mobile.isBlank() ? null : mobile,
                    dateOfBirth.orElse(null),
                    placeOfBirth.isBlank() ? null : placeOfBirth,
                    passNr.isBlank() ? null : passNr,
                    null
                );
            }
            user = user.withAddPosition(position);
            if (position.equals(DefaultPositions.POSITION_STM)) {
                user = user.withAddPosition(DefaultPositions.POSITION_MATROSE);
            }
//            var fitnessForSeaService = data[COL_FITNESS_FOR_SEA_SERVICE_EXPIRATION_DATE][r].trim();;
//            if (!fitnessForSeaService.isBlank() && !fitnessForSeaService.equals("-")) {
//                user.withAddQualification(new QualificationKey())
//            }
            users.put(key, user);
        }
        return users.values().stream().toList();
    }

    private static @NonNull PositionKey mapPosition(@NonNull String value) {
        var positionNormalized = value.toLowerCase()
            .replaceAll("[^a-zöäüß]", ""); // keep only a-z characters and a few symbols
        return switch (positionNormalized) {
            case "master" -> DefaultPositions.POSITION_KAPITAEN;
            case "kapitän" -> DefaultPositions.POSITION_KAPITAEN;

            case "mate" -> DefaultPositions.POSITION_STM;
            case "steuermann" -> DefaultPositions.POSITION_STM;

            case "noa" -> DefaultPositions.POSITION_NOA;
            case "moa" -> DefaultPositions.POSITION_NOA;
            case "cadet" -> DefaultPositions.POSITION_NOA;

            case "ab" -> DefaultPositions.POSITION_MATROSE;
            case "matrose" -> DefaultPositions.POSITION_MATROSE;
            case "m" -> DefaultPositions.POSITION_MATROSE;
            case "bosun" -> DefaultPositions.POSITION_MATROSE;
            case "abtrainer" -> DefaultPositions.POSITION_MATROSE;
            case "cadetab" -> DefaultPositions.POSITION_NOA;

            case "os" -> DefaultPositions.POSITION_LEICHTMATROSE;
            case "leichtmatrose" -> DefaultPositions.POSITION_LEICHTMATROSE;
            case "lm" -> DefaultPositions.POSITION_LEICHTMATROSE;
            case "oslm" -> DefaultPositions.POSITION_LEICHTMATROSE;

            case "engineer" -> DefaultPositions.POSITION_MASCHINIST;
            case "maschinist" -> DefaultPositions.POSITION_MASCHINIST;
            case "motorman" -> DefaultPositions.POSITION_MASCHINIST;
            case "motormann" -> DefaultPositions.POSITION_MASCHINIST;

            case "cook" -> DefaultPositions.POSITION_KOCH;
            case "koch" -> DefaultPositions.POSITION_KOCH;

            case "steward" -> DefaultPositions.POSITION_BACKSCHAFT;

            case "deckshand" -> DefaultPositions.POSITION_DECKSHAND;
            case "ostrainee" -> DefaultPositions.POSITION_DECKSHAND;
            case "ostainee" -> DefaultPositions.POSITION_DECKSHAND;

            // TODO add new position "Mitreisender"
            case "mitreisender" -> DefaultPositions.POSITION_DECKSHAND;
            case "supernumerary" -> DefaultPositions.POSITION_DECKSHAND;
            case "child" -> DefaultPositions.POSITION_DECKSHAND;
            case "purser" -> DefaultPositions.POSITION_DECKSHAND;
            case "" -> DefaultPositions.POSITION_DECKSHAND;

            default -> throw new IllegalArgumentException("Unknown position: " + value);
        };
    }
}
