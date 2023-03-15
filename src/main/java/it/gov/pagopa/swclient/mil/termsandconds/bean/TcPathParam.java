package it.gov.pagopa.swclient.mil.termsandconds.bean;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.PathParam;

import it.gov.pagopa.swclient.mil.termsandconds.ErrorCode;

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
