package it.gov.pagopa.swclient.mil.termsandconds.client;

import javax.validation.constraints.NotNull;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class TcResponse {
	/*
	 * T&C accepted or not
	 */
	@NotNull
	private String outcome;

	/**
	 * @return the outcome
	 */
	public String getOutcome() {
		return outcome;
	}

	/**
	 * @param outcome the outcome to set
	 */
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TcResponse [outcome=");
		builder.append(outcome);
		builder.append("]");
		return builder.toString();
	}
}
