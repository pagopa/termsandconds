package it.gov.pagopa.swclient.mil.termsandconds.bean;

public class TCVersion {

	private String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder .append("TCVersion [version=").append(version)
				.append("]");
		return builder.toString();
	}
}
