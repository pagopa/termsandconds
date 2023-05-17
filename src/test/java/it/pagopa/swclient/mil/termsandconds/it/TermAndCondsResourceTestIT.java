package it.pagopa.swclient.mil.termsandconds.it;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import it.pagopa.swclient.mil.termsandconds.resource.Initializer;
import it.pagopa.swclient.mil.termsandconds.resource.MongoTestResource;
import it.pagopa.swclient.mil.termsandconds.ErrorCode;
import it.pagopa.swclient.mil.termsandconds.resource.TermAndCondsResource;

@QuarkusIntegrationTest
@QuarkusTestResource(value=Initializer.class,restrictToAnnotatedClass = true)
@QuarkusTestResource(value=MongoTestResource.class,restrictToAnnotatedClass = true)
@TestHTTPEndpoint(TermAndCondsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TermAndCondsResourceTestIT {
	final static String SESSION_ID	= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String TAX_CODE	= "CHCZLN73D08A662B";
	final static String OUTCOME		= "TERMS_AND_CONDITIONS_NOT_YET_ACCEPTED";
	final static String TOKEN		= "XYZ13243XXYYZZ";
	final static String API_VERSION	= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	final static String TC_VERSION	= "1";
	
	Map<String, String> commonHeaders;
	
	@BeforeAll
	void createTestObjects() {
		commonHeaders = new HashMap<>();
		commonHeaders.put("RequestId", "d0d654e6-97da-4848-b568-99fedccb642b");
		commonHeaders.put("Version", API_VERSION);
		commonHeaders.put("AcquirerId", "4585625");
		commonHeaders.put("Channel", "ATM");
		commonHeaders.put("TerminalId", "0aB9wXyZ");
		commonHeaders.put("SessionId", "b0a000e6-97da-4848-b568-99fedccb641b");
		
	}
	
	//test GET API
	@Test
	void testGetTermsAndConds_200() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"id", SESSION_ID)
				.and()
				.when()
				.get("/CHCZLN73D08A662B")
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals("OK", response.jsonPath().getString("outcome"));
	}
	
	/* The current version is older than the current one. 
	 * Wiremock respond with the stub named PUT_PWD_TOKENIZER_DifferentVersion and the value ok the token is mappend in the mongo test db with a 
	 * different version */
	@Test
	void testGetTermsAndCondsTcVersionNotEquals_200() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"id", SESSION_ID)
				.and()
				.when()
				.get("/HHHZLN73D08A662B")
				.then()
				.extract()
				.response();
			
		 Assertions.assertEquals(200, response.statusCode());
		 Assertions.assertEquals("TERMS_AND_CONDITIONS_NOT_YET_ACCEPTED", response.jsonPath().getString("outcome"));
	}
	
	// test POST API
	@Test
	void testTermsAndConds_200() {
		commonHeaders.put("SessionId", SESSION_ID);
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(201, response.statusCode());
	        Assertions.assertEquals(true, response.jsonPath().getBoolean("saveNewCards"));
	}
	
	
	@Test
	void testTermsAndCondsSessionIdNotFound_404() {
		
		commonHeaders.put("AcquirerId", "4585625");
		commonHeaders.put("SessionId", "c0c444e6-97da-4848-b568-99fedccb642c");
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
	    Assertions.assertEquals(400, response.statusCode());
	    Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_SESSION_NOT_FOUND_SERVICE));
	    Assertions.assertNull(response.jsonPath().getJsonObject("saveNewCards"));
	}
	
	@Test
	void testTermsAndCondsGetSessionId_InternalServerError500() {
		commonHeaders.put("AcquirerId", "1111111");
		commonHeaders.put("SessionId", "c0c444e6-97da-4848-b568-99fedccb642c");
		Response response = given()
				.contentType(ContentType.JSON)
				
				.headers(commonHeaders)
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
	    Assertions.assertEquals(500, response.statusCode());
	    Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_SESSION_SERVICE));
	    Assertions.assertNull(response.jsonPath().getJsonObject("saveNewCards"));
	}
	
	
	/*
	 * Call the session stub GET_sessionWithSessionIdToGet500FromTokenizator that respond with  a "taxCode": "BBAFAL00D08A662C"
	 * mapped in the stab PUT_PWD_TOKENIZER_500.json responding internal server error.
	 */
	@Test
	void testManageOutcomeResponseCallingTokenizatorService_InternalServerError500() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
	    Assertions.assertEquals(500, response.statusCode());
	    Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_TOKENIZATOR_SERVICE));
	    Assertions.assertNull(response.jsonPath().getJsonObject("saveNewCards"));
	}
}
