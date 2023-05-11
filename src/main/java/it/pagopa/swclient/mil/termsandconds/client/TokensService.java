package it.pagopa.swclient.mil.termsandconds.client;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.termsandconds.client.bean.TokenRequest;
import it.pagopa.swclient.mil.termsandconds.client.bean.TokenResponse;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

/**
 * Reactive rest client for the REST APIs exposed by the PDV-Tokenizer service
 */
@Path("/tokens")
@RegisterRestClient(configKey = "tokens-api")
public interface TokensService {
	
	/**
	 * Client of the getToken API exposed by the PDV-Tokenizer
	 * Create a new token given a PII and Namespace, if already exists do nothing
	 * @param request JSon body
	 * @return the response from the PDV-Tokenizer
	 */
	@PUT
	@ClientHeaderParam(name = "x-api-key", value = "${x.api.key}", required = false)
    Uni<TokenResponse> getToken(TokenRequest request);

}
