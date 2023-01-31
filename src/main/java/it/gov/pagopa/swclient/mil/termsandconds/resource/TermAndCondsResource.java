/**
 * This module contains the REST endpoints exposed by the microservice.
 * 
 * @author Antonio Tarricone
 */
package it.gov.pagopa.swclient.mil.termsandconds.resource;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import com.mongodb.MongoSocketReadTimeoutException;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import it.gov.pagopa.swclient.mil.bean.CommonHeader;
import it.gov.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.termsandconds.ErrorCode;
import it.gov.pagopa.swclient.mil.termsandconds.bean.SaveNewCardsResponse;
import it.gov.pagopa.swclient.mil.termsandconds.bean.SessionRequest;
import it.gov.pagopa.swclient.mil.termsandconds.bean.SessionResponse;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TCHeaderParams;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TCVersion;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TcPathParam;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TokenBody;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TokenResponse;
import it.gov.pagopa.swclient.mil.termsandconds.client.PmWalletService;
import it.gov.pagopa.swclient.mil.termsandconds.client.SessionService;
import it.gov.pagopa.swclient.mil.termsandconds.client.TokensService;
import it.gov.pagopa.swclient.mil.termsandconds.dao.TCEntity;
import it.gov.pagopa.swclient.mil.termsandconds.dao.TCEntityVersion;
import it.gov.pagopa.swclient.mil.termsandconds.dao.TCRepository;
import it.gov.pagopa.swclient.mil.termsandconds.dao.TCVersionRepository;


@Path("/acceptedTermsConds")
public class TermAndCondsResource {
	
	/*
	 * Used to call the Session service to retrieve the 
	 * taxCode and the termsAndCondsAccepted values
	 */
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
	
	
	@GET
	@Path("/{taxCode}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Uni<Response> getTermsAndConds(@Valid @BeanParam CommonHeader headers,@Valid TcPathParam pathParams) {
		Log.debugf("getTermsAndConds - Input parameters: %s, taxCode: %s", headers, pathParams.getTaxCode());
		
		return retrieveVersion().chain(v -> manageGetTermsAndConditions(pathParams,v) );
		
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Uni<Response> acceptedTermsAndConds(@Valid @BeanParam TCHeaderParams headers) {
		Log.debugf("acceptedTermsAndConds - Input parameters: %s", headers);
		
		Log.debugf("acceptedTermsAndConds - call session service with session id= %s", headers.getSessionId());
		return sessionService.getSessionById(headers.getSessionId(), headers)
			.onFailure().transform(f -> {
				if ((f instanceof ClientWebApplicationException exc) && exc.getResponse().getStatus() == 404) {
					Log.errorf(f, "[%s] Error while retrieving terms and condition session Http Status code [%s] " , ErrorCode.ERROR_SESSION_NOT_FOUND_SERVICE,exc.getResponse().getStatus()) ;
					return new BadRequestException(Response
							.status(Status.BAD_REQUEST)
							.entity(new Errors(List.of(ErrorCode.ERROR_SESSION_NOT_FOUND_SERVICE)))
							.build());
				} else {
					Log.errorf(f, "[%s] Error while retrieving terms and condition session ", ErrorCode.ERROR_CALLING_SESSION_SERVICE);
					return new InternalServerErrorException(Response
						.status(Status.INTERNAL_SERVER_ERROR)
						.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_SESSION_SERVICE)))
						.build());
				}
			})
			.chain(c ->manageTokenResponse(c.getTaxCode()))
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
	 * @param taxCode
	 * @return tax code token or error
	 */
	private Uni<Tuple2<TokenResponse,String>> manageTokenResponse(String taxCode) {
		
		TokenBody tokenBody = new TokenBody();
		tokenBody.setPii(taxCode);
		
		return tokenService.getToken(tokenBody).onFailure().transform(t-> 
				{
					Log.errorf("[%s] Error calling tokenizator service ", ErrorCode.ERROR_CALLING_TOKENIZATOR_SERVICE);
					return new InternalServerErrorException(Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(new Errors(List.of(ErrorCode.ERROR_CALLING_TOKENIZATOR_SERVICE)))
							.build());
				})
				.map(token -> Tuple2.of(token, taxCode));
	}
		
	private Uni<TCEntity> manageUpinsert(final String taxCodeToken) {
		
		return retrieveVersion().map(v -> {
			TCEntity entity 	= new TCEntity();
			entity.taxCodeToken = taxCodeToken;
			
			TCVersion tcVersionObj	= new TCVersion();
			tcVersionObj.setVersion(v);
			
			entity.setVersion(tcVersionObj);
			Log.debugf("manageUpinsert - version= %s ", v);
			return tcRepository.persistOrUpdate(entity);
			
		}).chain(e -> e);
		
	}
	
	
	private Uni<SaveNewCardsResponse> saveNewCard(String taxCode,String apiVersion) {
		Log.debugf("saveNewCard");
		return pmWalletService.saveNewCards(taxCode, apiVersion)
				.onFailure().recoverWithItem( t -> {
					Log.errorf(t, "[%s] Error while saving card to pmWallet service", ErrorCode.ERROR_CALLING_TOKENIZATOR_SERVICE);
					SaveNewCardsResponse response =  new SaveNewCardsResponse();
					response.setSaveNewCards(false);
					return response;
				} )
				.map(token -> token );
		
	}
	
	private Uni<SaveNewCardsResponse> patchSaveNewCard(TCHeaderParams headers,SaveNewCardsResponse res) {
		Log.debugf("patchSaveNewCard - with saveNewCards %s and heades %s ", headers, res.isSaveNewCards());
		SessionRequest request = new SessionRequest();
		request.setSaveNewcards(res.isSaveNewCards());
		request.setTermsAndCondsAccepted(true);
		
		return sessionService.patchSessionById(headers.getSessionId(), headers, request)
			.onFailure().transform(t-> 
			{
				Log.errorf(t, "[%s] Error calling session service ", ErrorCode.ERROR_SAVING_SESSION_IN_SESSION_SERVICE);
				return new InternalServerErrorException(Response
						.status(Status.INTERNAL_SERVER_ERROR)
						.entity(new Errors(List.of(ErrorCode.ERROR_SAVING_SESSION_IN_SESSION_SERVICE)))
						.build());
			})
			.map(r -> res);
		
	}
	
	/**
	 * searches the token version by the tax code token and returns if the version is equals or not to the version in the property file
	 * @param taxCodeToken
	 * @return true if the version is equals to the one in the property field. False otherwise. Can rise a NotFoundException if no item is found.
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
									Log.errorf(t, "[%s] Error calling tokenizator service, Mongo Read Timeout ", ErrorCode.ERROR_TIMEOUT_MONGO_DB);
									errorCode = ErrorCode.ERROR_TIMEOUT_MONGO_DB;
								} else {
									Log.errorf(t, "[%s] Error calling tokenizator service ", ErrorCode.ERROR_SAVING_SESSION_IN_SESSION_SERVICE);
									errorCode = ErrorCode.ERROR_SAVING_SESSION_IN_SESSION_SERVICE;
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
						Log.errorf(t, "[%s] Internal server errorError calling tokenizator service ", ErrorCode.ERROR_VERSION_SERVICE);
						return new InternalServerErrorException(Response
								.status(Status.INTERNAL_SERVER_ERROR)
								.entity(new Errors(List.of(ErrorCode.ERROR_VERSION_SERVICE)))
								.build());
					}
				})
				.map(r -> {
					SessionResponse sessionResponse = new SessionResponse();
					if (r == Boolean.TRUE) {
						sessionResponse.setOutcome("OK");
						return Response.status(Status.OK).entity(sessionResponse).build();
					} else {
						sessionResponse.setOutcome("TERMS_AND_CONDITIONS_NOT_YET_ACCEPTED");	
						return Response.status(Status.NOT_FOUND).entity(sessionResponse).build();
					}
					
				} );
	}

}