package it.gov.pagopa.swclient.mil.termsandconds.client;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TokenBody;
import it.gov.pagopa.swclient.mil.termsandconds.bean.TokenResponse;

@Path("/tokens")
@RegisterRestClient(configKey = "tokens-api")
public interface TokensService {
	
	@PUT
    Uni<TokenResponse> getToken(TokenBody body);

}
