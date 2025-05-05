package ludo.mentis.aciem.mdc.util;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Implementation of ExcelHelper interface.
 * Provides utility methods for working with Excel cells and styles.
 */
@Component
public class ExcelHelperImpl implements ExcelHelper {
    private CellStyle dateCellStyle;
    private CellStyle dateTimeCellStyle;

    /**
     * {@inheritDoc}
     */
    public void init(Sheet sheet) {
        this.dateCellStyle = createDateCellStyle(sheet.getWorkbook());
        this.dateTimeCellStyle = createDateTimeCellStyle(sheet.getWorkbook());
    }

    /**
     * Creates a cell style for date values.
     *
     * @param workbook the workbook to create the style in
     * @return the created cell style
     */
    private CellStyle createDateCellStyle(Workbook workbook) {
        var cellStyle = workbook.createCellStyle();
        var createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
        return cellStyle;
    }

    /**
     * Creates a cell style for date and time values in the specified workbook.
     * The style formats cells with the pattern "dd/MM/yyyy HH:mm:ss".
     *
     * @param workbook the workbook in which the cell style is to be created. Must not be null.
     * @return the created cell style for date and time values
     */
    private CellStyle createDateTimeCellStyle(Workbook workbook) {
        var cellStyle = workbook.createCellStyle();
        var createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));
        return cellStyle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellValue(Row row, int column, String value) {
        if (value != null) {
            row.createCell(column).setCellValue(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellValue(Row row, int column, LocalDate value) {
        if (value != null) {
            var cell = row.createCell(column);
            cell.setCellValue(Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            cell.setCellStyle(dateCellStyle);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellValue(Row row, int column, LocalDateTime value) {
        if (value != null) {
            var cell = row.createCell(column);
            cell.setCellValue(Date.from(value.atZone(ZoneId.systemDefault()).toInstant()));
            cell.setCellStyle(dateTimeCellStyle);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellValue(Row row, int column, Number value) {
        if (value != null) {
            row.createCell(column).setCellValue(value.doubleValue());
        }
    }


}