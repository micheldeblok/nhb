package nl.mdb.nhb.nhclient.io;

import lombok.Data;

@Data
public class ApiVersion {

	private String api_version;
	
	public static class ApiVersionResponse extends AbstractResponse<ApiVersion> {}
}
