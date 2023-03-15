package it.gov.pagopa.swclient.mil.termsandconds.dao;

import org.bson.codecs.pojo.annotations.BsonId;

import io.quarkus.mongodb.panache.common.MongoEntity;

/**
 * Entity of the current T&C version
 */
@MongoEntity(database = "mil", collection = "termscondsversion")
public class TCEntityVersion {
	
	/*
	 * 
	 */
	@BsonId
	public String id;
	
	/*
	 * current T&C version
	 */
	private String version;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

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

}
