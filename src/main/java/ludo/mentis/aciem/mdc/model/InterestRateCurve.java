package ludo.mentis.aciem.mdc.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class InterestRateCurve {
	
	private LocalDate referenceDate;
	private String description;
	private BigDecimal beta1;
	private BigDecimal beta2;
	private BigDecimal beta3;
	private BigDecimal beta4;
	private BigDecimal lambda1;
	private BigDecimal lambda2;
	
	public InterestRateCurve() {
		// Required by some parsing frameworks
	}
	
	public LocalDate getReferenceDate() {
		return referenceDate;
	}
	
	public void setReferenceDate(LocalDate value) {
        this.referenceDate = value;
    }
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String value) {
		this.description = value;
	}

	public BigDecimal getBeta1() {
		return beta1;
	}
	
	public void setBeta1(BigDecimal value) {
		this.beta1 = value;
	}
	
	public BigDecimal getBeta2() {
		return beta2;
	}
	
	public void setBeta2(BigDecimal value) {
		this.beta2 = value;
	}
	
	public BigDecimal getBeta3() {
		return beta3;
	}
	
	public void setBeta3(BigDecimal value) {
		this.beta3 = value;
	}
	
	public BigDecimal getBeta4() {
		return beta4;
	}
	
	public void setBeta4(BigDecimal value) {
		this.beta4 = value;
	}
	
	public BigDecimal getLambda1() {
		return lambda1;
	}
	
	public void setLambda1(BigDecimal value) {
		this.lambda1 = value;
	}
	
	public BigDecimal getLambda2() {
		return lambda2;
	}
	
	public void setLambda2(BigDecimal value) {
		this.lambda2 = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(referenceDate, description, beta1, beta2, beta3, beta4, lambda1, lambda2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InterestRateCurve other = (InterestRateCurve) obj;
		return  Objects.equals(referenceDate, other.referenceDate) &&
				Objects.equals(description, other.description) &&
				Objects.equals(beta1, other.beta1) &&
				Objects.equals(beta2, other.beta2) &&
				Objects.equals(beta3, other.beta3) &&
				Objects.equals(beta4, other.beta4) &&
				Objects.equals(lambda1, other.lambda1) &&
				Objects.equals(lambda2, other.lambda2);
	}
	
	
}
