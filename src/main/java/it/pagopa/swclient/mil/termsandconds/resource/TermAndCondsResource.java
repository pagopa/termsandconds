/**
 * This module contains the REST endpoints exposed by the microservice.
 * 
 * @author Antonio Tarricone
 */
package it.pagopa.swclient.mil.termsandconds.resource;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import com.mongodb.MongoSocketReadTimeoutException;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.bean.Errors;
import it.pagopa.swclient.mil.termsandconds.ErrorCode;
import it.pagopa.swclient.mil.termsandconds.bean.TCHeaderParams;
import it.pagopa.swclient.mil.termsandconds.bean.TCVersion;
import it.pagopa.swclient.mil.termsandconds.bean.TcPathParam;
import it.pagopa.swclient.mil.termsandconds.client.PmWalletService;
import it.pagopa.swclient.mil.termsandconds.client.SessionService;
import it.pagopa.swclient.mil.termsandconds.client.TcResponse;
import it.pagopa.swclient.mil.termsandconds.client.TokensService;
import it.pagopa.swclient.mil.termsandconds.client.bean.SaveNewCardsResponse;
import it.pagopa.swclient.mil.termsandconds.client.bean.SessionRequest;
import it.pagopa.swclient.mil.termsandconds.client.bean.TokenRequest;
import it.pagopa.swclient.mil.termsandconds.client.bean.TokenResponse;
import it.pagopa.swclient.mil.termsandconds.dao.TCEntityVersion;
import it.pagopa.swclient.mil.termsandconds.dao.TCRepository;
import it.pagopa.swclient.mil.termsandconds.dao.TCVersionRepository;
import it.pagopa.swclient.mil.termsandconds.dao.TcTaxCodeTokenEntity;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;


@Path("/acceptedTermsConds")
public class TermAndCondsResource {

	@RestClient
	private SessionService sessionService;
	
	@RestClient
	private TokensService tokenService;
	
	@RestClient
	private PmWalletService pmWalletService;
	
	@Inject
	private TCRepository tcRepository;
	
	@Inject
	private TCVersionRepository tcVersionRepository;
	
	/**
	 * Check the acceptance of T&C for the user identified by the tax code
	 * @param headers a set of mandatory headers
	 * @param pathParams {@link TcPathParam} containing the tax code of the user
	 * @return the value of the acceptance of T&C as "OK" or "TERMS_AND_CONDITIONS_NOT_YET_ACCEPTED" values
	 */
	@GET
	@Path("/{taxCode}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Uni<Response> getTermsAndConds(@Valid @BeanParam CommonHeader headers,@Valid TcPathParam pathParams) {
		Log.debugf("getTermsAndConds - Input parameters: %s, taxCode: %s", headers, pathParams.getTaxCode());
		
		return retrieveVersion().chain(v -> manageGetTermsAndConditions(pathParams,v) );
		
	}
	
	/**
	 * Store the acceptance of T&C for the user identified by the tax code stored in the session
	 * @param headers headers a set of mandatory headers
	 * @return true if the client should invoke the presave operation of the Wallet microservice
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Uni<Response> acceptedTermsAndConds(@Valid @BeanParam TCHeaderParams headers) {
		Log.debugf("acceptedTermsAndConds - Input parameters: %s", headers);
		
		Log.debugf("acceptedTermsAndConds - call session service with session id= %s", headers.getSessionId());
		return sessionService.getSessionById(headers.getSessionId(), headers)
			.onFailure().transform(f -> {
				if ((f instanceof ClientWebApplicationException exc) && exc.getResponse().getStatus() == 404) {
					Log.errorf(f, "[%s] Error session not found" , ErrorCode.ERROR_SESSION_NOT_FOUND_SERVICE,exc.getResponse().getStatus()) ;
					return new BadRequestException(Response
							.status(Status.BAD_REQUEST)
							.entity(new Errors(List.of(ErrorCode.ERROR_SESSION_NOT_FOUND_SERVICE)))
							.build());
				} else {
					Log.errorf(f, "[%s] Error while retrieving session by Id ", ErrorCode.ERROR_CALLING_SESSION_SERVICE);
					return new InternalServerErrorException(Response
						.status(Status.INTERNAL_SERVER_ERROR)
						.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_SESSION_SERVICE)))
						.build());
				}
			})
			.chain(c -> {
				Log.debugf("acceptedTermsAndConds - Session Response %s", c.toString());
				return manageTokenResponse(c.getTaxCode());
			})
			.call(save -> manageUpinsert(save.getItem1().getToken()))
			.chain(c -> saveNewCard(c.getItem2(), headers.getVersion()))
			.chain(c -> patchSaveNewCard(headers, c))
			.map(m -> {
				Log.debugf("Response %s",m);
				return Response.status(Status.CREATED).entity(m).build();
			});
	
	}
	
	/**
	 * Call the PDV-Tokenizer passing the tax code as body and manage the response getting the tax code token
	 * @param taxCode of the user
	 * @return tax code token or error
	 */
	private Uni<Tuple2<TokenResponse,String>> manageTokenResponse(String taxCode) {
		
		TokenRequest tokenRequest = new TokenRequest();
		tokenRequest.setPii(taxCode);
		
		return tokenService.getToken(tokenRequest).onFailure().transform(t-> 
				{
					Log.errorf("[%s] Error calling tokenizator service ", ErrorCode.ERROR_CALLING_TOKENIZATOR_SERVICE);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_TOKENIZATOR_SERVICE)))
							.build());
				})
				.map(token -> Tuple2.of(token, taxCode));
	}
	
	/**
	 * saves or update the T&C
	 * @param taxCodeToken: token returned by the PDV-Tokenizer service
	 * @return the saved value
	 */
	private Uni<TcTaxCodeTokenEntity> manageUpinsert(final String taxCodeToken) {
		
		return retrieveVersion().map(v -> {
			TcTaxCodeTokenEntity entity 	= new TcTaxCodeTokenEntity();
			entity.taxCodeToken = taxCodeToken;
			
			TCVersion tcVersionObj	= new TCVersion();
			tcVersionObj.setVersion(v);
			Log.debugf("manageUpinsert - Value of TCVersion to save %s", tcVersionObj.toString());
			entity.setVersion(tcVersionObj);
			Log.debugf("manageUpinsert - version= %s ", v);
			return tcRepository.persistOrUpdate(entity);
			
		}).chain(e -> e);
		
	}
	
	/**
	 * Call the PM-Wallet saveNewCard API
	 * @param taxCode of the user
	 * @param apiVersion
	 * @return the value to know if the client should invoke the presave operation of the Wallet microservice
	 */
	private Uni<SaveNewCardsResponse> saveNewCard(String taxCode,String apiVersion) {
		Log.debugf("saveNewCard - Calling saveNewCard API ");
		return pmWalletService.saveNewCards(taxCode, apiVersion)
				.onFailure().recoverWithItem( t -> {
					Log.errorf(t, "[%s] Error while calling saveNewCard API to pmWallet service", ErrorCode.ERROR_CALLING_PW_WALLET_SERVICE);
					SaveNewCardsResponse response =  new SaveNewCardsResponse();
					response.setSaveNewCards(false);
					return response;
				} )
				.map(token -> {
					Log.debugf("saveNewCard API responded with [%s]", token);
					return token;	
				} );
		
	}
	
	/**
	 * Call the session service to update partially a session by ID
	 * @param headers
	 * @param res
	 * @return
	 */
	private Uni<SaveNewCardsResponse> patchSaveNewCard(TCHeaderParams headers,SaveNewCardsResponse res) {
		Log.debugf("patchSaveNewCard - with saveNewCards %s and heades %s ", headers, res.isSaveNewCards());
		SessionRequest request = new SessionRequest();
		request.setSaveNewcards(res.isSaveNewCards());
		request.setTermsAndCondsAccepted(true);
		Log.debugf("patchSaveNewCard - Calling session service with request %s", request.toString());
		return sessionService.patchSessionById(headers.getSessionId(), headers, request)
			.onFailure().transform(t-> 
			{
				Log.errorf(t, "[%s] Error calling session service to save session ", ErrorCode.ERROR_SAVING_SESSION_IN_SESSION_SERVICE);
				return new InternalServerErrorException(Response
						.status(Status.INTERNAL_SERVER_ERROR)
						.entity(new Errors(List.of(ErrorCode.ERROR_SAVING_SESSION_IN_SESSION_SERVICE)))
						.build());
			})
			.map(r -> res);
		
	}
	
	/**
	 * Checks the version retrieving it by the tax code token and T&C version
	 * @param taxCodeToken token returned by the PDV-Tokenizer service
	 * @param tcVersion T&C version
	 * @return  true if the current version is equals to older one. False otherwise.
	 */
	private Uni<Boolean> manageFindVersionByTaxCode(String taxCodeToken, String tcVersion) {
		Log.debugf("manageFindVersionByTaxCode - find version by taxCodeToken: [%s] ", taxCodeToken);
		
		return tcRepository.findByIdOptional(taxCodeToken)
				 .onItem().transform(o -> o.orElseThrow(() -> 
				 			new NotFoundException(Response
								.status(Status.NOT_FOUND)
								.build())
						 )).map(t -> t.getVersion().getVersion().equals(tcVersion)); //check if the version is equals or not 

	}
	
	private Uni<String> retrieveVersion() {
		Log.debugf("Retrieve version from DB");
		return tcVersionRepository.findByIdOptional("tcVersion")
				 .onFailure().transform(t->
							{ 
								String errorCode = "";
								if (t instanceof MongoSocketReadTimeoutException) {
									Log.errorf(t, "[%s] Error retrieving T&C version, Mongo Read Timeout ", ErrorCode.ERROR_TIMEOUT_MONGO_DB);
									errorCode = ErrorCode.ERROR_TIMEOUT_MONGO_DB;
								} else {
									Log.errorf(t, "[%s] Error retrieving T&C version ", ErrorCode.ERROR_RETRIEVING_TC_VERSION);
									errorCode = ErrorCode.ERROR_RETRIEVING_TC_VERSION;
								}
								return new InternalServerErrorException(Response
										.status(Status.INTERNAL_SERVER_ERROR)
										.entity(new Errors(List.of(errorCode)))
										.build());
							})
				 .onItem().transform(o -> o.orElseThrow(() -> {
					 Log.errorf("Version not found in the DB ");
				 			return new NotFoundException(Response
								.status(Status.NOT_FOUND)
								.build());
				 	})).map(TCEntityVersion::getVersion);
	}
	
	/**
	 * Retrieves the tokens calling the PDV-Tokenizer service passing the tax code of the user.
	 * Checks if the terms and condition version is older than the current one. 
	 * @param pathParams
	 * @param version
	 * @return the value of the acceptance of T&C as "OK" or "TERMS_AND_CONDITIONS_NOT_YET_ACCEPTED" values
	 */
	private Uni<Response> manageGetTermsAndConditions(TcPathParam pathParams, String version) {
		return manageTokenResponse(pathParams.getTaxCode())
				.chain(f -> manageFindVersionByTaxCode(f.getItem1().getToken(),version))
				.onFailure().transform(t-> 
				{
					if (t instanceof NotFoundException) {
						Log.errorf(t, "Version not found in the DB");
						return new NotFoundException(Response
								.status(Status.NOT_FOUND)
								.entity(new Errors(List.of(ErrorCode.ERROR_VERSION_NOT_FOUND_SERVICE)))
								.build());
					} else {
						Log.errorf(t, "[%s] Error retrieving version ", ErrorCode.ERROR_VERSION_SERVICE);
						return new InternalServerErrorException(Response
								.status(Status.INTERNAL_SERVER_ERROR)
								.entity(new Errors(List.of(ErrorCode.ERROR_VERSION_SERVICE)))
								.build());
					}
				})
				.map(r -> {
					TcResponse tcResponse = new TcResponse();
					if (r == Boolean.TRUE) {
						Log.debugf("The current T&C is the same of the older one", 0);
						tcResponse.setOutcome("OK");
						Log.debugf("getTermsAndConds response %s", tcResponse.toString());
						return Response.status(Status.OK).entity(tcResponse).build();
					} else {
						Log.debugf("The current T&C is not the same of the older one", 0);
						tcResponse.setOutcome("TERMS_AND_CONDITIONS_NOT_YET_ACCEPTED");
						Log.debugf("getTermsAndConds response %s", tcResponse.toString());
						return Response.status(Status.OK).entity(tcResponse).build();
					}
				} );
	}
}