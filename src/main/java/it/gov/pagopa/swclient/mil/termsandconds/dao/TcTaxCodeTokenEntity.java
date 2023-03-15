package it.gov.pagopa.swclient.mil.termsandconds.dao;

import org.bson.codecs.pojo.annotations.BsonId;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TCVersion;

/**
 * Entity of the T&C version by taxCodeToken
 */
@MongoEntity(database = "mil", collection = "termsconds")
public class TcTaxCodeTokenEntity {
	
	/*
	 * token retrieved by taxCode from the PDV-Tokenizer 
	 */
	@BsonId
	public String taxCodeToken;
	
	/*
	 * Object rappresenting the last T&C version saved associated to the taxCodeToken
	 */
	private TCVersion version;

	/**
	 * @return the taxCodeToken
	 */
	public String getTaxCodeToken() {
		return taxCodeToken;
	}

	/**
	 * @param taxCodeToken the taxCodeToken to set
	 */
	public void setTaxCodeToken(String taxCodeToken) {
		this.taxCodeToken = taxCodeToken;
	}

	/**
	 * @return the version
	 */
	public TCVersion getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(TCVersion version) {
		this.version = version;
	}
}
