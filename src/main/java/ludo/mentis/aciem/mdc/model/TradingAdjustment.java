package ludo.mentis.aciem.mdc.model;

import java.math.BigDecimal;
import java.util.Objects;

public class TradingAdjustment {

    private String commodity;
    private String maturity;
    private BigDecimal previousAdjustmentPrice;
    private BigDecimal currentAdjustmentPrice;
    private BigDecimal variation;
    private BigDecimal adjustmentValuePerContract;

    public TradingAdjustment() {
    }

    public TradingAdjustment(String commodity, String maturity, BigDecimal previousAdjustmentPrice, BigDecimal currentAdjustmentPrice, BigDecimal variation, BigDecimal adjustmentValuePerContract) {
        this.commodity = commodity;
        this.maturity = maturity;
        this.previousAdjustmentPrice = previousAdjustmentPrice;
        this.currentAdjustmentPrice = currentAdjustmentPrice;
        this.variation = variation;
        this.adjustmentValuePerContract = adjustmentValuePerContract;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String value) {
        this.commodity = value;
    }

    public String getMaturity() {
        return maturity;
    }

    public void setMaturity(String value) {
        this.maturity = value;
    }

    public BigDecimal getPreviousAdjustmentPrice() {
        return previousAdjustmentPrice;
    }

    public void setPreviousAdjustmentPrice(BigDecimal value) {
        this.previousAdjustmentPrice = value;
    }

    public BigDecimal getCurrentAdjustmentPrice() {
        return currentAdjustmentPrice;
    }

    public void setCurrentAdjustmentPrice(BigDecimal value) {
        this.currentAdjustmentPrice = value;
    }

    public BigDecimal getVariation() {
        return variation;
    }

    public void setVariation(BigDecimal value) {
        this.variation = value;
    }

    public BigDecimal getAdjustmentValuePerContract() {
        return adjustmentValuePerContract;
    }

    public void setAdjustmentValuePerContract(BigDecimal value) {
        this.adjustmentValuePerContract = value;
    }

	@Override
	public int hashCode() {
		return Objects.hash(adjustmentValuePerContract, commodity, currentAdjustmentPrice, maturity,
				previousAdjustmentPrice, variation);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TradingAdjustment other = (TradingAdjustment) obj;
		return     Objects.equals(adjustmentValuePerContract, other.adjustmentValuePerContract)
				&& Objects.equals(commodity, other.commodity)
				&& Objects.equals(currentAdjustmentPrice, other.currentAdjustmentPrice)
				&& Objects.equals(maturity, other.maturity)
				&& Objects.equals(previousAdjustmentPrice, other.previousAdjustmentPrice)
				&& Objects.equals(variation, other.variation);
	}
    
    
}