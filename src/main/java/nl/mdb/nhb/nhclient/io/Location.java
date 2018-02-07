package nl.mdb.nhb.nhclient.io;

import lombok.Getter;

public enum Location {

	EU(0), US(1);
	
	@Getter
	private int code;
	
	private Location(int code) {
		this.code = code;
	}
}
