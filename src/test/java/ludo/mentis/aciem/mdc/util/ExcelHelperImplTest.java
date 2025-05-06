package ludo.mentis.aciem.mdc.util;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class ExcelHelperImplTest {

    private ExcelHelperImpl excelHelper;
    private Sheet sheet;

    @BeforeEach
    void setUp() {
        excelHelper = new ExcelHelperImpl();
        var workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Test Sheet");
    }

    @Test
    void init_shouldCreateCellStyles() {
        // When
        excelHelper.init(sheet);

        // Then
        // We can't directly test private fields, but we can test the behavior
        // by setting values that use these styles
        var row = sheet.createRow(0);
        
        // This will use the date style
        excelHelper.setCellValue(row, 0, LocalDate.of(2023, 5, 15));
        var dateCell = row.getCell(0);
        assertNotNull(dateCell);
        assertEquals(CellType.NUMERIC, dateCell.getCellType());
        assertNotNull(dateCell.getCellStyle());
        
        // This will use the dateTime style
        excelHelper.setCellValue(row, 1, LocalDateTime.of(2023, 5, 15, 10, 30, 0));
        var dateTimeCell = row.getCell(1);
        assertNotNull(dateTimeCell);
        assertEquals(CellType.NUMERIC, dateTimeCell.getCellType());
        assertNotNull(dateTimeCell.getCellStyle());
        
        // The styles should be different
        assertNotEquals(dateCell.getCellStyle().getIndex(), dateTimeCell.getCellStyle().getIndex());
    }

    @Test
    void setCellValue_shouldSetStringValue() {
        // Given
        excelHelper.init(sheet);
        var row = sheet.createRow(0);
        var value = "Test String";

        // When
        excelHelper.setCellValue(row, 0, value);

        // Then
        var cell = row.getCell(0);
        assertNotNull(cell);
        assertEquals(CellType.STRING, cell.getCellType());
        assertEquals(value, cell.getStringCellValue());
    }

    @Test
    void setCellValue_shouldSetLocalDateValue() {
        // Given
        excelHelper.init(sheet);
        var row = sheet.createRow(0);
        var value = LocalDate.of(2023, Month.MAY, 15);

        // When
        excelHelper.setCellValue(row, 0, value);

        // Then
        var cell = row.getCell(0);
        assertNotNull(cell);
        assertEquals(CellType.NUMERIC, cell.getCellType());
        
        // Convert back to LocalDate for comparison
        var cellDate = cell.getDateCellValue();
        var cellLocalDate = cellDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        assertEquals(value, cellLocalDate);
    }

    @Test
    void setCellValue_shouldSetLocalDateTimeValue() {
        // Given
        excelHelper.init(sheet);
        var row = sheet.createRow(0);
        var value = LocalDateTime.of(2023, Month.MAY, 15, 10, 30, 45);

        // When
        excelHelper.setCellValue(row, 0, value);

        // Then
        var cell = row.getCell(0);
        assertNotNull(cell);
        assertEquals(CellType.NUMERIC, cell.getCellType());
        
        // Convert back to LocalDateTime for comparison (ignoring seconds for simplicity)
        var cellDate = cell.getDateCellValue();
        var cellLocalDateTime = cellDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(value.getYear(), cellLocalDateTime.getYear());
        assertEquals(value.getMonth(), cellLocalDateTime.getMonth());
        assertEquals(value.getDayOfMonth(), cellLocalDateTime.getDayOfMonth());
        assertEquals(value.getHour(), cellLocalDateTime.getHour());
        assertEquals(value.getMinute(), cellLocalDateTime.getMinute());
    }

    @Test
    void setCellValue_shouldSetNumberValue() {
        // Given
        excelHelper.init(sheet);
        var row = sheet.createRow(0);
        
        // Test with different number types
        Integer intValue = 42;
        var doubleValue = 3.14159;
        var bigDecimalValue = new BigDecimal("123456.789");

        // When
        excelHelper.setCellValue(row, 0, intValue);
        excelHelper.setCellValue(row, 1, doubleValue);
        excelHelper.setCellValue(row, 2, bigDecimalValue);

        // Then
        var intCell = row.getCell(0);
        assertNotNull(intCell);
        assertEquals(CellType.NUMERIC, intCell.getCellType());
        assertEquals(intValue.doubleValue(), intCell.getNumericCellValue(), 0.0001);

        var doubleCell = row.getCell(1);
        assertNotNull(doubleCell);
        assertEquals(CellType.NUMERIC, doubleCell.getCellType());
        assertEquals(doubleValue, doubleCell.getNumericCellValue(), 0.0001);

        var bigDecimalCell = row.getCell(2);
        assertNotNull(bigDecimalCell);
        assertEquals(CellType.NUMERIC, bigDecimalCell.getCellType());
        assertEquals(bigDecimalValue.doubleValue(), bigDecimalCell.getNumericCellValue(), 0.0001);
    }

    @Test
    void setCellValue_shouldHandleNullValues() {
        // Given
        excelHelper.init(sheet);
        var row = sheet.createRow(0);

        // When
        excelHelper.setCellValue(row, 0, (String) null);
        excelHelper.setCellValue(row, 1, (LocalDate) null);
        excelHelper.setCellValue(row, 2, (LocalDateTime) null);
        excelHelper.setCellValue(row, 3, (Number) null);

        // Then
        // No cells should be created for null values
        assertNull(row.getCell(0));
        assertNull(row.getCell(1));
        assertNull(row.getCell(2));
        assertNull(row.getCell(3));
    }

    @Test
    void setCellValue_shouldCreateCellIfNotExists() {
        // Given
        excelHelper.init(sheet);
        var row = sheet.createRow(0);
        
        // When
        excelHelper.setCellValue(row, 5, "Test Value"); // Column 5 doesn't exist yet
        
        // Then
        var cell = row.getCell(5);
        assertNotNull(cell);
        assertEquals("Test Value", cell.getStringCellValue());
    }
}