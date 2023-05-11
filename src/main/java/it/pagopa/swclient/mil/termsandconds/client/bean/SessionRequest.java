package it.pagopa.swclient.mil.termsandconds.client.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SessionRequest {
	
	/*
	 * status of terms and condition 
	 */
	private boolean termsAndCondsAccepted;

	/*
	 * If true the client should invoke the presave operation of the Wallet microservice
	 */
	private boolean saveNewcards;

	/**
	 * @return the termsAndCondsAccepted
	 */
	public boolean isTermsAndCondsAccepted() {
		return termsAndCondsAccepted;
	}

	/**
	 * @param termsAndCondsAccepted the termsAndCondsAccepted to set
	 */
	public void setTermsAndCondsAccepted(boolean termsAndCondsAccepted) {
		this.termsAndCondsAccepted = termsAndCondsAccepted;
	}

	/**
	 * @return the saveNewcards
	 */
	public boolean isSaveNewcards() {
		return saveNewcards;
	}

	/**
	 * @param saveNewcards the saveNewcards to set
	 */
	public void setSaveNewcards(boolean saveNewcards) {
		this.saveNewcards = saveNewcards;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder .append("SessionRequest [termsAndCondsAccepted=").append(termsAndCondsAccepted)
				.append(" saveNewcards=").append(saveNewcards)
				.append("]");
		return builder.toString();
	}
}
