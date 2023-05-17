package it.pagopa.swclient.mil.termsandconds.client.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public class TokenRequest {
	
	/*
	 * Personal Identifiable Information, the tax code of the user
	 */
	@NotNull
	private String pii;

	/**
	 * @return the pii
	 */
	public String getPii() {
		return pii;
	}

	/**
	 * @param pii the pii to set
	 */
	public void setPii(String pii) {
		this.pii = pii;
	}
}
