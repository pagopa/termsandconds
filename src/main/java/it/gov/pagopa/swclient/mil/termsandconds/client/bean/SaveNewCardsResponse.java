package it.gov.pagopa.swclient.mil.termsandconds.client.bean;

import javax.validation.constraints.NotNull;

public class SaveNewCardsResponse {

	/*
	 *  Value to indicate if the client should invoke the presave operation of the Wallet microservice
	 */
	@NotNull
	private boolean saveNewCards;

	/**
	 * @return the saveNewCards
	 */
	public boolean isSaveNewCards() {
		return saveNewCards;
	}

	/**
	 * @param saveNewCards the saveNewCards to set
	 */
	public void setSaveNewCards(boolean saveNewCards) {
		this.saveNewCards = saveNewCards;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SaveNewCardsResponse [saveNewCards=");
		builder.append(saveNewCards);
		builder.append("]");
		return builder.toString();
	}
}
