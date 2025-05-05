package ludo.mentis.aciem.mdc.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class FinancialIndicator {

    private Long securityIdentificationCode;
    private String description;
    private String groupDescription;
    private BigDecimal value;
    private BigDecimal rate;
    private LocalDate lastUpdate;

    public FinancialIndicator() {
    }

    public Long getSecurityIdentificationCode() {
        return securityIdentificationCode;
    }

    public void setSecurityIdentificationCode(Long value) {
        this.securityIdentificationCode = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String value) {
        this.groupDescription = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal value) {
        this.rate = value;
    }

    public LocalDate getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDate value) {
        this.lastUpdate = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FinancialIndicator that = (FinancialIndicator) o;
        return  Objects.equals(securityIdentificationCode, that.securityIdentificationCode) &&
                Objects.equals(description, that.description) &&
                Objects.equals(groupDescription, that.groupDescription) &&
                Objects.equals(value, that.value) &&
                Objects.equals(rate, that.rate) &&
                Objects.equals(lastUpdate, that.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(securityIdentificationCode, description, groupDescription, value, rate, lastUpdate);
    }
}
