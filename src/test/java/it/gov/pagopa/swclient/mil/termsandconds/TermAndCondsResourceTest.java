package it.gov.pagopa.swclient.mil.termsandconds;

import static io.restassured.RestAssured.given;

import java.util.Optional;

import javax.ws.rs.InternalServerErrorException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.bean.CommonHeader;
import it.gov.pagopa.swclient.mil.termsandconds.bean.SaveNewCardsResponse;
import it.gov.pagopa.swclient.mil.termsandconds.bean.SessionRequest;
import it.gov.pagopa.swclient.mil.termsandconds.bean.SessionResponse;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TCVersion;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TokenBody;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TokenResponse;
import it.gov.pagopa.swclient.mil.termsandconds.dao.TCEntity;
import it.gov.pagopa.swclient.mil.termsandconds.dao.TCRepository;
import it.gov.pagopa.swclient.mil.termsandconds.resource.PmWalletService;
import it.gov.pagopa.swclient.mil.termsandconds.resource.SessionService;
import it.gov.pagopa.swclient.mil.termsandconds.resource.TermAndCondsResource;
import it.gov.pagopa.swclient.mil.termsandconds.resource.TokensService;

@QuarkusTest
@TestHTTPEndpoint(TermAndCondsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TermAndCondsResourceTest {
	
	final static String SESSION_ID	= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String TAX_CODE	= "CHCZLN73D08A662B";
	final static String OUTCOME		= "TERMS_AND_CONDITIONS_NOT_YET_ACCEPTED";
	final static String TOKEN		= "XYZ13243XXYYZZ";
	final static String API_VERSION	= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	final static String TC_VERSION	= "1";
	
	@InjectMock
	@RestClient
	private SessionService sessionService;
	
	@InjectMock
	@RestClient
	private TokensService tokenService;
	
	@InjectMock
	private TCRepository tcRepository;
	
	@InjectMock
	@RestClient
	private PmWalletService pmWalletService;
	
	@ConfigProperty(name="terms.conds")
	private String tcVersion;
	
	// test POST API
	@Test
	void testTermsAndConds_200() {
		SessionResponse sessionResponse = new SessionResponse();
		sessionResponse.setTaxCode(TAX_CODE);
		sessionResponse.setOutcome(OUTCOME);

		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TCEntity tcEntity = new TCEntity();
		
		SaveNewCardsResponse saveNewCardsResponse = new SaveNewCardsResponse();
		saveNewCardsResponse.setSaveNewCards(true);
		
		
		Mockito
		.when(sessionService.getSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class)))
		.thenReturn(Uni.createFrom().item(sessionResponse));
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenBody.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcRepository.persistOrUpdate(Mockito.any(TCEntity.class)))
		.thenReturn(Uni.createFrom().item(tcEntity));
		
		Mockito
		.when(pmWalletService.saveNewCards(TAX_CODE, API_VERSION))
		.thenReturn(Uni.createFrom().item(saveNewCardsResponse));

		Mockito
		.when(sessionService.patchSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class), Mockito.any(SessionRequest.class)))
		.thenReturn(Uni.createFrom().item(saveNewCardsResponse));
		
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
		Mockito
		.when(sessionService.getSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class)))
		.thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(404)));
		
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
		
	    Assertions.assertEquals(400, response.statusCode());
	    Assertions.assertEquals("{\"errors\":[\"004000005\"]}", response.getBody().asString());
	        
	     
	}

	@Test
	void testTermsAndCondsGetSessionId_InternalServerError500() {
		Mockito
		.when(sessionService.getSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class)))
		.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
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
		
	    Assertions.assertEquals(500, response.statusCode());
	    Assertions.assertEquals("{\"errors\":[\"004000003\"]}", response.getBody().asString());
	     
	}
	
	@Test
	void testManageOutcomeResponseCallingTokenizatorService_InternalServerError500() {
		SessionResponse sessionResponse = new SessionResponse();
		sessionResponse.setTaxCode(TAX_CODE);
		sessionResponse.setOutcome(OUTCOME);
		
		Mockito
		.when(sessionService.getSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class)))
		.thenReturn(Uni.createFrom().item(sessionResponse));
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenBody.class)))
		.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
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
		
	    Assertions.assertEquals(500, response.statusCode());
	    Assertions.assertEquals("{\"errors\":[\"004000004\"]}", response.getBody().asString());
	     
	}
	
	@Test
	void testSaveNewCard_Failure() {
		SessionResponse sessionResponse = new SessionResponse();
		sessionResponse.setTaxCode(TAX_CODE);
		sessionResponse.setOutcome(OUTCOME);
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TCEntity tcEntity = new TCEntity();
		
		Mockito
		.when(sessionService.getSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class)))
		.thenReturn(Uni.createFrom().item(sessionResponse));
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenBody.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcRepository.persistOrUpdate(Mockito.any(TCEntity.class)))
		.thenReturn(Uni.createFrom().item(tcEntity));
		
		Mockito
		.when(pmWalletService.saveNewCards(TAX_CODE, API_VERSION))
		.thenReturn(Uni.createFrom().failure(new ClientWebApplicationException(500)));
		
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
	      Assertions.assertEquals(false, response.jsonPath().getBoolean("saveNewCards"));
	    
	}
	
	// test GET API
	@Test
	void testGetTermsAndConds_200() {
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TCVersion tcV = new TCVersion();
		tcV.setVersion(tcVersion);
		TCEntity tcEntity = new TCEntity();
		tcEntity.version = tcV;

		Mockito
		.when(tokenService.getToken(Mockito.any(TokenBody.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));

		Mockito
		.when(tcRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntity)));
		
		
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals("{\"outcome\":\"OK\"}", response.getBody().asString());
	     
	}
	
	@Test
	void testGetTermsAndCondsTcVersionNotEquals_200() {
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TCVersion tcV = new TCVersion();
		tcV.setVersion("0000");
		TCEntity tcEntity = new TCEntity();
		tcEntity.version = tcV;

		Mockito
		.when(tokenService.getToken(Mockito.any(TokenBody.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));

		Mockito
		.when(tcRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntity)));
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(404, response.statusCode());
	        Assertions.assertEquals("{\"outcome\":\"TERMS_AND_CONDITIONS_NOT_YET_ACCEPTED\"}", response.getBody().asString());
	     
	}
	
	@Test
	void testGetTermsAndConds_404() {
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenBody.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.empty()));
		
		
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(404, response.statusCode());
	        Assertions.assertEquals("{\"errors\":[\"004000006\"]}", response.getBody().asString());
	     
	}

	
	@Test
	void testGetTermsAndConds_500() {
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenBody.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(500, response.statusCode());
	        Assertions.assertEquals("{\"errors\":[\"004000007\"]}", response.getBody().asString());
	     
	}
}
