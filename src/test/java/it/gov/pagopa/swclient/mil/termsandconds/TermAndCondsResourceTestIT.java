package it.gov.pagopa.swclient.mil.termsandconds;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import it.gov.pagopa.swclient.mil.termsandconds.resource.TermAndCondsResource;

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
						"SessionId", SESSION_ID)
				.and()
				.when()
				.get("/CHCZLN73D08A662B")
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals("{\"outcome\":\"OK\"}", response.getBody().asString());
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
						"SessionId", SESSION_ID)
				.and()
				.when()
				.get("/HHHZLN73D08A662B")
				.then()
				.extract()
				.response();
			
		 Assertions.assertEquals(404, response.statusCode());
	     Assertions.assertEquals("{\"outcome\":\"TERMS_AND_CONDITIONS_NOT_YET_ACCEPTED\"}", response.getBody().asString());
	}
	
	@Test
	void testGetTermsAndConds_404() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.when()
				.get("/LLOZLN73D08A662A")
				.then()
				.extract()
				.response();
			
		 Assertions.assertEquals(404, response.statusCode());
		 Assertions.assertEquals("{\"errors\":[\"004000006\"]}", response.getBody().asString());
	}

	
	// test POST API
	@Test
	void testTermsAndConds_200() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
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
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", "c0c444e6-97da-4848-b568-99fedccb642c")
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
	    Assertions.assertEquals(400, response.statusCode());
	    Assertions.assertEquals("{\"errors\":[\"004000005\"]}", response.getBody().asString());
	}
	
	@Test
	void testTermsAndCondsGetSessionId_InternalServerError500() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "1111111",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", "c0c444e6-97da-4848-b568-99fedccb642c")
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
	    Assertions.assertEquals(500, response.statusCode());
	    Assertions.assertEquals("{\"errors\":[\"004000003\"]}", response.getBody().asString());
	}
	
	
	/*
	 * Call the session stub GET_sessionWithSessionIdToGet500FromTokenizator that respond with  a "taxCode": "BBAFAL00D08A662C"
	 * mapped in the stab PUT_PWD_TOKENIZER_500.json responding internal server error.
	 */
	@Test
	void testManageOutcomeResponseCallingTokenizatorService_InternalServerError500() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", "b0a000e6-97da-4848-b568-99fedccb641b")
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
	    Assertions.assertEquals(500, response.statusCode());
	    Assertions.assertEquals("{\"errors\":[\"004000004\"]}", response.getBody().asString());
	}
	

	
}
