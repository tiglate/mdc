package ludo.mentis.aciem.mdc.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class ExchangeRateParity {
    private LocalDate referenceDate;
    private String currencyId;
    private String type;
    private String currencyCode;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private BigDecimal buyParity;
    private BigDecimal sellParity;

    public ExchangeRateParity() {
        // default constructor for Spring Batch
    }

    public BigDecimal getBuyParity() {
        return buyParity;
    }

    public void setBuyParity(BigDecimal value) {
        this.buyParity = value;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate value) {
        this.referenceDate = value;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String value) {
        this.currencyId = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String value) {
        this.currencyCode = value;
    }

    public BigDecimal getBuyRate() {
        return buyRate;
    }

    public void setBuyRate(BigDecimal value) {
        this.buyRate = value;
    }

    public BigDecimal getSellRate() {
        return sellRate;
    }

    public void setSellRate(BigDecimal value) {
        this.sellRate = value;
    }

    public BigDecimal getSellParity() {
        return sellParity;
    }

    public void setSellParity(BigDecimal value) {
        this.sellParity = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRateParity that = (ExchangeRateParity) o;
        return  Objects.equals(referenceDate, that.referenceDate) &&
                Objects.equals(currencyId, that.currencyId) &&
                Objects.equals(type, that.type) &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(buyRate, that.buyRate) &&
                Objects.equals(sellRate, that.sellRate) &&
                Objects.equals(buyParity, that.buyParity) &&
                Objects.equals(sellParity, that.sellParity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceDate, currencyId, type, currencyCode, buyRate, sellRate, buyParity, sellParity);
    }
}
