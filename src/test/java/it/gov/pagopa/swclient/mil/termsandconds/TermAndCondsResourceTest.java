package it.gov.pagopa.swclient.mil.termsandconds;

import static io.restassured.RestAssured.given;

import java.util.Optional;

import javax.ws.rs.InternalServerErrorException;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.ServerAddress;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.bean.CommonHeader;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TCVersion;
import it.gov.pagopa.swclient.mil.termsandconds.client.PmWalletService;
import it.gov.pagopa.swclient.mil.termsandconds.client.SessionService;
import it.gov.pagopa.swclient.mil.termsandconds.client.TokensService;
import it.gov.pagopa.swclient.mil.termsandconds.client.bean.SaveNewCardsResponse;
import it.gov.pagopa.swclient.mil.termsandconds.client.bean.SessionRequest;
import it.gov.pagopa.swclient.mil.termsandconds.client.bean.SessionResponse;
import it.gov.pagopa.swclient.mil.termsandconds.client.bean.TokenRequest;
import it.gov.pagopa.swclient.mil.termsandconds.client.bean.TokenResponse;
import it.gov.pagopa.swclient.mil.termsandconds.dao.TCEntityVersion;
import it.gov.pagopa.swclient.mil.termsandconds.dao.TCRepository;
import it.gov.pagopa.swclient.mil.termsandconds.dao.TCVersionRepository;
import it.gov.pagopa.swclient.mil.termsandconds.dao.TcTaxCodeTokenEntity;
import it.gov.pagopa.swclient.mil.termsandconds.resource.TermAndCondsResource;

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
	private TCVersionRepository tcVersionRepository;
	
	@InjectMock
	@RestClient
	private PmWalletService pmWalletService;
	
	
	// test POST API
	@Test
	void testTermsAndConds_200() {
		SessionResponse sessionResponse = new SessionResponse();
		sessionResponse.setTaxCode(TAX_CODE);
		sessionResponse.setOutcome(OUTCOME);

		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TcTaxCodeTokenEntity tcEntity = new TcTaxCodeTokenEntity();
		
		SaveNewCardsResponse saveNewCardsResponse = new SaveNewCardsResponse();
		saveNewCardsResponse.setSaveNewCards(true);
		
		TCEntityVersion tcEntityVersion = new TCEntityVersion();
		tcEntityVersion.setVersion("1");
		
		Mockito
		.when(sessionService.getSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class)))
		.thenReturn(Uni.createFrom().item(sessionResponse));
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcRepository.persistOrUpdate(Mockito.any(TcTaxCodeTokenEntity.class)))
		.thenReturn(Uni.createFrom().item(tcEntity));
		
		Mockito
		.when(tcVersionRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntityVersion)));
		
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
						"id", SESSION_ID)
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
	void testTermsAndConds_500_savingSession() {
		SessionResponse sessionResponse = new SessionResponse();
		sessionResponse.setTaxCode(TAX_CODE);
		sessionResponse.setOutcome(OUTCOME);

		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TcTaxCodeTokenEntity tcEntity = new TcTaxCodeTokenEntity();
		
		SaveNewCardsResponse saveNewCardsResponse = new SaveNewCardsResponse();
		saveNewCardsResponse.setSaveNewCards(true);
		
		TCEntityVersion tcEntityVersion = new TCEntityVersion();
		tcEntityVersion.setVersion("1");
		
		Mockito
		.when(sessionService.getSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class)))
		.thenReturn(Uni.createFrom().item(sessionResponse));
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcRepository.persistOrUpdate(Mockito.any(TcTaxCodeTokenEntity.class)))
		.thenReturn(Uni.createFrom().item(tcEntity));
		
		Mockito
		.when(tcVersionRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntityVersion)));
		
		Mockito
		.when(pmWalletService.saveNewCards(TAX_CODE, API_VERSION))
		.thenReturn(Uni.createFrom().item(saveNewCardsResponse));

		Mockito
		.when(sessionService.patchSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class), Mockito.any(SessionRequest.class)))
		.thenReturn(Uni.createFrom().failure(new Exception()));
		
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
				.post()
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(500, response.statusCode());
			Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_SAVING_SESSION_IN_SESSION_SERVICE));
			Assertions.assertNull(response.jsonPath().getJsonObject("saveNewCards"));
	     
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
						"id", SESSION_ID)
				
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
						"id", SESSION_ID)
				
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
	
	@Test
	void testManageOutcomeResponseCallingTokenizatorService_InternalServerError500() {
		SessionResponse sessionResponse = new SessionResponse();
		sessionResponse.setTaxCode(TAX_CODE);
		sessionResponse.setOutcome(OUTCOME);
		
		Mockito
		.when(sessionService.getSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class)))
		.thenReturn(Uni.createFrom().item(sessionResponse));
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
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
				.post()
				.then()
				.extract()
				.response();
		
	    Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CALLING_TOKENIZATOR_SERVICE));
		Assertions.assertNull(response.jsonPath().getJsonObject("saveNewCards"));
	     
	}
	
	@Test
	void testSaveNewCard_Failure() {
		SessionResponse sessionResponse = new SessionResponse();
		sessionResponse.setTaxCode(TAX_CODE);
		sessionResponse.setOutcome(OUTCOME);
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TcTaxCodeTokenEntity tcEntity = new TcTaxCodeTokenEntity();
		
		TCEntityVersion tcEntityVersion = new TCEntityVersion();
		tcEntityVersion.setVersion("1");
		
		Mockito
		.when(sessionService.getSessionById(Mockito.eq(SESSION_ID), Mockito.any(CommonHeader.class)))
		.thenReturn(Uni.createFrom().item(sessionResponse));
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcRepository.persistOrUpdate(Mockito.any(TcTaxCodeTokenEntity.class)))
		.thenReturn(Uni.createFrom().item(tcEntity));
		
		Mockito
		.when(tcVersionRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntityVersion)));
		
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
						"id", SESSION_ID)
				
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
		
		TCEntityVersion tcEntityVersion = new TCEntityVersion();
		tcEntityVersion.setVersion("1");
		
		TCVersion tcV = new TCVersion();
		tcV.setVersion("1");
		TcTaxCodeTokenEntity tcEntity = new TcTaxCodeTokenEntity();
		tcEntity.setVersion(tcV);

		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));

		Mockito
		.when(tcRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntity)));
		
		Mockito
		.when(tcVersionRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntityVersion)));
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals("OK", response.jsonPath().getJsonObject("outcome"));
	}
	
	@Test
	void testGetTermsAndCondsTcVersionNotEquals_200() {
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TCEntityVersion tcEntityVersion = new TCEntityVersion();
		tcEntityVersion.setVersion("1");
		
		TCVersion tcV = new TCVersion();
		tcV.setVersion("0000");
		TcTaxCodeTokenEntity tcEntity = new TcTaxCodeTokenEntity();
		tcEntity.setVersion(tcV);

		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));

		Mockito
		.when(tcRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntity)));
		
		Mockito
		.when(tcVersionRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntityVersion)));
		
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals("TERMS_AND_CONDITIONS_NOT_YET_ACCEPTED", response.jsonPath().getJsonObject("outcome"));
	}
	
	@Test
	void testGetTermsAndConds_404_versionNotFound() {
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TCEntityVersion tcEntityVersion = new TCEntityVersion();
		tcEntityVersion.setVersion("1");
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.empty()));
		
		Mockito
		.when(tcVersionRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntityVersion)));
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(404, response.statusCode());
			Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_VERSION_NOT_FOUND_SERVICE));
			Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
	     
	}
	
	@Test
	void testGetTermsAndConds_500_retrievingVersion() {
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TCEntityVersion tcEntityVersion = new TCEntityVersion();
		tcEntityVersion.setVersion("1");
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
		Mockito
		.when(tcVersionRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(tcEntityVersion)));
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(500, response.statusCode());
			Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_VERSION_SERVICE));
			Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
	}
	
	@Test
	void testGetTermsAndConds_500_mongoSocketTimeout() {
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TCEntityVersion tcEntityVersion = new TCEntityVersion();
		tcEntityVersion.setVersion("1");
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcVersionRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().failure(new MongoSocketReadTimeoutException("",new ServerAddress("localhost"),new Exception())));
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(500, response.statusCode());
			Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_TIMEOUT_MONGO_DB));
			Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
	}
	
	@Test
	void testGetTermsAndConds_500_retrievingTcVersion() {
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TCEntityVersion tcEntityVersion = new TCEntityVersion();
		tcEntityVersion.setVersion("1");
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcVersionRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().failure(new Exception()));
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(500, response.statusCode());
			Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_RETRIEVING_TC_VERSION));
			Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
	} 
	
	@Test
	void testGetTermsAndConds_404_retrievingTcVersion_NotFoundInTheDb() {
		
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(TOKEN);
		
		TCEntityVersion tcEntityVersion = new TCEntityVersion();
		tcEntityVersion.setVersion("1");
		
		Mockito
		.when(tokenService.getToken(Mockito.any(TokenRequest.class)))
		.thenReturn(Uni.createFrom().item(tokenResponse));
		
		Mockito
		.when(tcVersionRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.empty()));
		
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
				.get("/"+TAX_CODE)
				.then()
				.extract()
				.response();
			
	        Assertions.assertEquals(404, response.statusCode());
//			Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.NOT_FOUND));
//			Assertions.assertNull(response.jsonPath().getJsonObject("outcome"));
	} 
}
