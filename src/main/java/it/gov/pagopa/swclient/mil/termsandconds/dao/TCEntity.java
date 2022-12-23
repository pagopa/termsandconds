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
	
	public TCVersion version;
	
}
