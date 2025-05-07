package ludo.mentis.aciem.mdc.reader;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import ludo.mentis.aciem.mdc.model.TradingAdjustment;

public class TradingAdjustmentsHtmlReader implements ItemReader<TradingAdjustment> {
	
	private static Logger log = LoggerFactory.getLogger(TradingAdjustmentsHtmlReader.class);

    private Iterator<TradingAdjustment> dataIterator;
    private boolean initialized = false;
    private final byte[] htmlFileContent; // Inject this

    public TradingAdjustmentsHtmlReader(byte[] htmlFileContent) {
        this.htmlFileContent = htmlFileContent;
    }

    @Override
    public TradingAdjustment read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
        	dataIterator = parseHtmlTable().iterator();
        	this.initialized = true;
        }

        if (dataIterator != null && dataIterator.hasNext()) {
            return dataIterator.next();
        } else {
            return null; // Signals end of data
        }
    }
    
    private List<TradingAdjustment> parseHtmlTable() throws IOException {
    	var resultList = new ArrayList<TradingAdjustment>();
        var htmlContentString = new String(this.htmlFileContent, "windows-1252");
        var doc = Jsoup.parse(htmlContentString);

        var table = doc.getElementById("tblDadosAjustes");
        if (table == null) {
        	throw new IOException("Table with id 'tblDadosAjustes' not found.");
        }

        var rows = table.select("tbody > tr");
        var currentCommodity = "";

        for (var row : rows) {
            var cols = row.select("td");
            var rowText = row.text();

            String commodity;
            int maturityIndex;
            int prevAdjustmentPriceIndex;
            int currentAdjustmentPriceIndex;
            int variationIndex;
            int adjustmentValueIndex;

            if (cols.size() == 6) { // Row has a new commodity
            	currentCommodity = cols.get(0).text().isBlank() ? currentCommodity : cols.get(0).text().trim();
                commodity = currentCommodity;
                maturityIndex = 1;
                prevAdjustmentPriceIndex = 2;
                currentAdjustmentPriceIndex = 3;
                variationIndex = 4;
                adjustmentValueIndex = 5;
            } else if (cols.size() == 5) { // Row uses the commodity from the previous row with rowspan
                commodity = currentCommodity;
                maturityIndex = 0;
                prevAdjustmentPriceIndex = 1;
                currentAdjustmentPriceIndex = 2;
                variationIndex = 3;
                adjustmentValueIndex = 4;
            } else {
                // Skip rows that don't match the expected structure
                log.warn("Skipping row with unexpected column count: {} | Row content: {}", cols.size(), rowText);
                continue;
            }

            try {
                var maturity = cols.get(maturityIndex).text().trim();
                var prevAdjustmentPrice = parseBigDecimal(cols.get(prevAdjustmentPriceIndex).text());
                var currentAdjustmentPrice = parseBigDecimal(cols.get(currentAdjustmentPriceIndex).text());
                var variation = parseBigDecimal(cols.get(variationIndex).text());
                var adjustmentValue = parseBigDecimal(cols.get(adjustmentValueIndex).text());

                resultList.add(new TradingAdjustment(commodity, maturity, prevAdjustmentPrice, currentAdjustmentPrice, variation, adjustmentValue));
            } catch (NumberFormatException e) {
                throw new ParseException("Error parsing number in row: " + row.text(), e);
            } catch (IndexOutOfBoundsException e) {
            	throw new ParseException("Error accessing column in row (IndexOutOfBounds): " + row.text(), e);
            }
        }

        return resultList;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty() || value.equals("-")) {
            return BigDecimal.ZERO; // Or handle as null/throw exception as per your requirement
        }
        // Remove thousand separators (dots) and use comma as decimal separator
        return new BigDecimal(value.trim().replace(".", "").replace(",", "."));
    }
}