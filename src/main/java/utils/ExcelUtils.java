package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ExcelUtils — Read test data from .xlsx files (Apache POI)
 *
 * Usage:
 *   String value = ExcelUtils.getCellData("TestData/sample_data.xlsx", "LoginData", 1, 0);
 *   Object[][] data = ExcelUtils.getTableArray("TestData/sample_data.xlsx", "LoginData");
 */
public class ExcelUtils {

    /**
     * Get value of a specific cell
     *
     * @param filePath  Path to xlsx file
     * @param sheetName Sheet name
     * @param rowNum    Row index (0-based; row 0 = header)
     * @param colNum    Column index (0-based)
     * @return Cell value as String
     */
    public static String getCellData(String filePath, String sheetName, int rowNum, int colNum) {
        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new RuntimeException("Sheet not found: " + sheetName);

            Row row = sheet.getRow(rowNum);
            if (row == null) return "";

            Cell cell = row.getCell(colNum);
            if (cell == null) return "";

            return getCellValueAsString(cell);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel: " + filePath, e);
        }
    }

    /**
     * Get total number of data rows (excluding header row 0)
     */
    public static int getRowCount(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new RuntimeException("Sheet not found: " + sheetName);
            return sheet.getLastRowNum(); // excludes header

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel: " + filePath, e);
        }
    }

    /**
     * Get total number of columns in header row
     */
    public static int getColumnCount(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) return 0;
            Row header = sheet.getRow(0);
            return header == null ? 0 : header.getLastCellNum();

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel: " + filePath, e);
        }
    }

    /**
     * Get entire sheet as 2D Object array — for use with @DataProvider
     * Skips header row (row 0)
     *
     * @param filePath  Path to xlsx file
     * @param sheetName Sheet name
     * @return Object[][] where each row is one test data set
     */
    public static Object[][] getTableArray(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new RuntimeException("Sheet not found: " + sheetName);

            int rowCount = sheet.getLastRowNum();         // total rows (0-based, excluding header)
            int colCount = sheet.getRow(0).getLastCellNum();

            Object[][] data = new Object[rowCount][colCount];

            for (int r = 1; r <= rowCount; r++) {
                Row row = sheet.getRow(r);
                for (int c = 0; c < colCount; c++) {
                    data[r - 1][c] = (row != null && row.getCell(c) != null)
                        ? getCellValueAsString(row.getCell(c))
                        : "";
                }
            }
            return data;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel: " + filePath, e);
        }
    }

    /**
     * Get a specific column as a List of Strings
     *
     * @param filePath  Path to xlsx file
     * @param sheetName Sheet name
     * @param colIndex  Column index (0-based)
     * @return List of values in that column (excluding header)
     */
    public static List<String> getColumnData(String filePath, String sheetName, int colIndex) {
        List<String> values = new ArrayList<>();
        int rows = getRowCount(filePath, sheetName);
        for (int r = 1; r <= rows; r++) {
            values.add(getCellData(filePath, sheetName, r, colIndex));
        }
        return values;
    }

    // ===== Private helper =====
    private static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                double d = cell.getNumericCellValue();
                // return as integer if no decimal
                return (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            case BLANK:   return "";
            default:      return "";
        }
    }
}
