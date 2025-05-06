package ludo.mentis.aciem.mdc.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Interface for Excel helper operations.
 * Provides methods for working with Excel cells and styles.
 */
public interface ExcelHelper {

    /**
     * Initializes the ExcelHelper with the given sheet.
     *
     * @param sheet the sheet to work with. Must not be null.
     */
    void init(Sheet sheet);

    /**
     * Sets a String value in a cell.
     *
     * @param row    the row where the cell is located
     * @param column the column index of the cell
     * @param value  the String value to set
     */
    void setCellValue(Row row, int column, String value);

    /**
     * Sets a LocalDate value in a cell.
     *
     * @param row    the row where the cell is located
     * @param column the column index of the cell
     * @param value  the LocalDate value to set
     */
    void setCellValue(Row row, int column, LocalDate value);

    /**
     * Sets a LocalDateTime value in a specified cell in the given row and column index.
     * The value is formatted with a specific date-time style.
     *
     * @param row    the row where the cell is located. Must not be null.
     * @param column the column index of the cell to set the value. Must be a non-negative integer.
     * @param value  the LocalDateTime value to set in the cell. If null, the cell remains unset.
     */
    void setCellValue(Row row, int column, LocalDateTime value);

    /**
     * Sets a numeric value in a cell.
     *
     * @param row    the row where the cell is located
     * @param column the column index of the cell
     * @param value  the Number value to set
     */
    void setCellValue(Row row, int column, Number value);
}