package it.gov.pagopa.swclient.mil.termsandconds.client;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.termsandconds.bean.SaveNewCardsResponse;

@RegisterRestClient(configKey = "pmwallet-api")
public interface PmWalletService {
	
	
	@GET
	@Path("/enabledServices/{taxCode}/saveNewCards")
    Uni<SaveNewCardsResponse> saveNewCards(@PathParam("taxCode") String taxCode, @HeaderParam("Version") String version);

}
