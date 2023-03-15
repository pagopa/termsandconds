package it.gov.pagopa.swclient.mil.termsandconds.client.bean;

import javax.validation.constraints.NotNull;

public class TokenResponse {
	
	/*
	 * Namespaced token related to the PII
	 */
	@NotNull
	private String token;

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

}