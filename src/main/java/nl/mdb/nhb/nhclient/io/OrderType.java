package nl.mdb.nhb.nhclient.io;

import lombok.Getter;

public enum OrderType {

	STANDARD(0), FIXED(1);
	
	@Getter
	private int code;
	
	private OrderType(int code) {
		this.code = code;
	}
}
