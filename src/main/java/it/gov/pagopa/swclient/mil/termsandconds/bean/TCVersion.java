package it.gov.pagopa.swclient.mil.termsandconds.bean;

import it.gov.pagopa.swclient.mil.termsandconds.dao.TcTaxCodeTokenEntity;

/**
 * Represent the last T&C version saved associated to the taxCodeToken used in the {@link TcTaxCodeTokenEntity}
 */
public class TCVersion {

	/*
	 * API version
	 */
	private String version;

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
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
