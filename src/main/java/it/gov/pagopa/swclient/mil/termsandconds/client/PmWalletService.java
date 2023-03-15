package it.gov.pagopa.swclient.mil.termsandconds.client;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.termsandconds.client.bean.SaveNewCardsResponse;

/**
 * Reactive rest client for the REST APIs exposed by the PM-Wallet service
 */
@RegisterRestClient(configKey = "pmwallet-api")
public interface PmWalletService {

	/**
	 * Client of the enabledServices API exposed by the PM-Wallet
	 * Get the value of the flag "save new cards"
	 * @param taxCode of the user
	 * @param version of the API
	 * @return the response from the PM-Wallet
	 */
	@GET
	@Path("/enabledServices/{taxCode}/saveNewCards")
	@ClientHeaderParam(name = "Ocp-Apim-Subscription-Key", value = "${ocp.apim.subscription}", required = false)
    Uni<SaveNewCardsResponse> saveNewCards(@NotNull @PathParam("taxCode") String taxCode, @HeaderParam("Version") String version);

}
