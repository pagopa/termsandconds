package it.gov.pagopa.swclient.mil.termsandconds.dao;

import org.bson.codecs.pojo.annotations.BsonId;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TCVersion;

@MongoEntity(database = "mil", collection = "termsconds")
public class TCEntity {
	/*
	 * 
	 */
	@BsonId
	public String taxCodeToken;
	
	private TCVersion version;

	public TCVersion getVersion() {
		return version;
	}

	public void setVersion(TCVersion version) {
		this.version = version;
	}
	
}
