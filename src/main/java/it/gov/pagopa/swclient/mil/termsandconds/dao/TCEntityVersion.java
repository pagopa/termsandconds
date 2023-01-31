package it.gov.pagopa.swclient.mil.termsandconds.dao;

import org.bson.codecs.pojo.annotations.BsonId;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(database = "mil", collection = "termscondsversion")
public class TCEntityVersion {
	/*
	 * 
	 */
	@BsonId
	public String id;
	
	private String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
}
