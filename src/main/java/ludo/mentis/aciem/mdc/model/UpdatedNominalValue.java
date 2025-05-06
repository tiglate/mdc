package ludo.mentis.aciem.mdc.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.aot.generate.Generated;

@Generated
public class UpdatedNominalValue {

	private LocalDate referenceDate;
	private String security;
	private String selicCode;
	private BigDecimal value;
	private BigDecimal index;
	private String reference;
	private LocalDate validSince;
	
	public UpdatedNominalValue() {
		// Required by some parsing engines
	}
	
	public LocalDate getReferenceDate() {
        return referenceDate;
    }
	
	public void setReferenceDate(LocalDate value) {
        this.referenceDate = value;
    }

	public String getSecurity() {
		return security;
	}

	public void setSecurity(String value) {
		this.security = value;
	}

	public String getSelicCode() {
		return selicCode;
	}

	public void setSelicCode(String value) {
		this.selicCode = value;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public BigDecimal getIndex() {
		return index;
	}

	public void setIndex(BigDecimal value) {
		this.index = value;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String value) {
		this.reference = value;
	}

	public LocalDate getValidSince() {
		return validSince;
	}

	public void setValidSince(LocalDate value) {
		this.validSince = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(referenceDate, index, reference, security, selicCode, validSince, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UpdatedNominalValue other = (UpdatedNominalValue) obj;
		return  Objects.equals(referenceDate, other.referenceDate) &&
				Objects.equals(index, other.index) &&
				Objects.equals(reference, other.reference) &&
				Objects.equals(security, other.security) &&
				Objects.equals(selicCode, other.selicCode) &&
				Objects.equals(validSince, other.validSince) &&
				Objects.equals(value, other.value);
	}
	
	
}
