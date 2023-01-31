package it.gov.pagopa.swclient.mil.termsandconds.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class TokenBody {
	private String pii;

	public String getPii() {
		return pii;
	}

	public void setPii(String pii) {
		this.pii = pii;
	}
}
