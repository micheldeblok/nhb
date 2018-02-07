package nl.mdb.nhb.nhclient.io;

import lombok.Data;

@Data
public abstract class AbstractResponse<T> {

	T result;
	String method;
}
