package ludo.mentis.aciem.mdc.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.aot.generate.Generated;

@Generated
public class BrazilianBondPrice {
    private String title;
    private LocalDate referenceDate;
    private String selicCode;
    private LocalDate baseDate;
    private LocalDate maturityDate;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private BigDecimal indicativeRate;
    private BigDecimal price;
    private BigDecimal standardDeviation;
    private BigDecimal lowerIntervalD0;
    private BigDecimal upperIntervalD0;
    private BigDecimal lowerIntervalD1;
    private BigDecimal upperIntervalD1;
    private String criteria;

    public BrazilianBondPrice() {
        // default constructor for Spring Batch
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    public String getSelicCode() {
        return selicCode;
    }

    public void setSelicCode(String selicCode) {
        this.selicCode = selicCode;
    }

    public LocalDate getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(LocalDate baseDate) {
        this.baseDate = baseDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public BigDecimal getBuyRate() {
        return buyRate;
    }

    public void setBuyRate(BigDecimal buyRate) {
        this.buyRate = buyRate;
    }

    public BigDecimal getSellRate() {
        return sellRate;
    }

    public void setSellRate(BigDecimal sellRate) {
        this.sellRate = sellRate;
    }

    public BigDecimal getIndicativeRate() {
        return indicativeRate;
    }

    public void setIndicativeRate(BigDecimal indicativeRate) {
        this.indicativeRate = indicativeRate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(BigDecimal standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public BigDecimal getLowerIntervalD0() {
        return lowerIntervalD0;
    }

    public void setLowerIntervalD0(BigDecimal lowerIntervalD0) {
        this.lowerIntervalD0 = lowerIntervalD0;
    }

    public BigDecimal getUpperIntervalD0() {
        return upperIntervalD0;
    }

    public void setUpperIntervalD0(BigDecimal upperIntervalD0) {
        this.upperIntervalD0 = upperIntervalD0;
    }

    public BigDecimal getLowerIntervalD1() {
        return lowerIntervalD1;
    }

    public void setLowerIntervalD1(BigDecimal lowerIntervalD1) {
        this.lowerIntervalD1 = lowerIntervalD1;
    }

    public BigDecimal getUpperIntervalD1() {
        return upperIntervalD1;
    }

    public void setUpperIntervalD1(BigDecimal upperIntervalD1) {
        this.upperIntervalD1 = upperIntervalD1;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BrazilianBondPrice that = (BrazilianBondPrice) o;
        return  Objects.equals(title, that.title) &&
                Objects.equals(referenceDate, that.referenceDate) &&
                Objects.equals(selicCode, that.selicCode) &&
                Objects.equals(baseDate, that.baseDate) &&
                Objects.equals(maturityDate, that.maturityDate) &&
                Objects.equals(buyRate, that.buyRate) &&
                Objects.equals(sellRate, that.sellRate) &&
                Objects.equals(indicativeRate, that.indicativeRate) &&
                Objects.equals(price, that.price) &&
                Objects.equals(standardDeviation, that.standardDeviation) &&
                Objects.equals(lowerIntervalD0, that.lowerIntervalD0) &&
                Objects.equals(upperIntervalD0, that.upperIntervalD0) &&
                Objects.equals(lowerIntervalD1, that.lowerIntervalD1) &&
                Objects.equals(upperIntervalD1, that.upperIntervalD1) &&
                Objects.equals(criteria, that.criteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, referenceDate, selicCode, baseDate, maturityDate, buyRate, sellRate, indicativeRate,
                price, standardDeviation, lowerIntervalD0, upperIntervalD0, lowerIntervalD1, upperIntervalD1, criteria);
    }
}