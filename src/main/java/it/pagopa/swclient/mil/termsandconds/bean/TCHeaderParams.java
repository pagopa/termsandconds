package it.pagopa.swclient.mil.termsandconds.bean;

import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.termsandconds.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.HeaderParam;

public class TCHeaderParams extends CommonHeader{

	/*
	 * Request ID
	 */
	@HeaderParam("SessionId")
	@NotNull(message = "[" + ErrorCode.SESSION_ID_MUST_NOT_BE_NULL + "] RequestId must not be null")
	@Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$", message = "[" + ErrorCode.SESSION_ID_MUST_MATCH_REGEXP + "] RequestId must match \"{regexp}\"")
	private String sessionId;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TCHeaderParams [sessionId=");
		builder.append(sessionId);
		builder.append("]");
		return builder.toString();
	}

}
