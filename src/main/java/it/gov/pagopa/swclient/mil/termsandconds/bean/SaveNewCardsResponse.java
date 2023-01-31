package it.gov.pagopa.swclient.mil.termsandconds.bean;

public class SaveNewCardsResponse {

	private boolean saveNewCards;

	public boolean isSaveNewCards() {
		return saveNewCards;
	}

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
