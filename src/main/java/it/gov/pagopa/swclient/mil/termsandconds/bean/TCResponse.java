/**
 * This module contains the beans that model the data handled by the microservice.
 * 
 * @author Antonio Tarricone
 */
package it.gov.pagopa.swclient.mil.termsandconds.bean;
public class TCResponse {
	private String saveNewCards;

	public String getSaveNewCards() {
		return saveNewCards;
	}

	public void setSaveNewCards(String saveNewCards) {
		this.saveNewCards = saveNewCards;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TermsAndConds [saveNewCards=").append(saveNewCards).append("]");
		return builder.toString();
	}
	
}