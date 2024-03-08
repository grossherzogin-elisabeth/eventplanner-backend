package org.eventplanner.webapp.utils;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class ExcelUtils {

    public static String[][] readExcelFile(File file) throws IOException {
        if (!file.exists()) {
            return new String[][]{};
        }
        try(InputStream in = new FileInputStream(file)) {
            return readExcelFile(in);
        }
    }

    public static String[][] readExcelFile(InputStream in) throws IOException {
        var workbook = new XSSFWorkbook(in);
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
            // cell value is not a date, ignore
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
