package it.pagopa.swclient.mil.termsandconds.bean;

import it.pagopa.swclient.mil.termsandconds.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.PathParam;

public class TcPathParam {

	/*
	 * tax code of the user
	 */
	@PathParam(value = "taxCode")
	@NotNull(message = "[" + ErrorCode.TAX_CODE_MUST_NOT_BE_NULL + "] taxCode must not be null")
	@Pattern(regexp = "[a-zA-Z]{6}\\d{2}[a-zA-Z]\\d{2}[a-zA-Z]\\d{3}[a-zA-Z]", message = "[" + ErrorCode.TAX_CODE_MUST_MATCH_REGEXP + "] taxCode must match \"{regexp}\"")
	private String taxCode;

	/**
	 * @return the taxCode
	 */
	public String getTaxCode() {
		return taxCode;
	}

	/**
	 * @param taxCode the taxCode to set
	 */
	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

}
