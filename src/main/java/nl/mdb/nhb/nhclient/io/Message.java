package nl.mdb.nhb.nhclient.io;

import lombok.Data;

@Data
public class Message {

	private String success;
	
	public static class MessageResponse extends AbstractResponse<Message> {}
}
