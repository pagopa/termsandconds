package it.gov.pagopa.swclient.mil.termsandconds.bean;

public class TokenBody {
	private String pii;

	public String getPii() {
		return pii;
	}

	public void setPii(String pii) {
		this.pii = pii;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder .append("TokenBody response [pii=").append(pii)
				.append("]");
		return builder.toString();
	}
}
