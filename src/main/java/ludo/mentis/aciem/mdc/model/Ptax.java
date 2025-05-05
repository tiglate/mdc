package ludo.mentis.aciem.mdc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Ptax {
    @JsonProperty("cotacaoCompra")
    private BigDecimal buyValue;

    @JsonProperty("cotacaoVenda")
    private BigDecimal sellValue;

    @JsonProperty("dataHoraCotacao")
    private LocalDateTime timestamp;

    public Ptax() {
    }

    public BigDecimal getBuyValue() {
        return buyValue;
    }

    public void setBuyValue(BigDecimal buyValue) {
        this.buyValue = buyValue;
    }

    public BigDecimal getSellValue() {
        return sellValue;
    }

    public void setSellValue(BigDecimal sellValue) {
        this.sellValue = sellValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ptax ptax = (Ptax) o;
        return Objects.equals(buyValue, ptax.buyValue) &&
               Objects.equals(sellValue, ptax.sellValue) &&
               Objects.equals(timestamp, ptax.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buyValue, sellValue, timestamp);
    }
}
