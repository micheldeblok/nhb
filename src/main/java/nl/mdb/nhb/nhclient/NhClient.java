package nl.mdb.nhb.nhclient;

import java.math.BigDecimal;

import nl.mdb.nhb.nhclient.io.Algo;
import nl.mdb.nhb.nhclient.io.BuyInfo;
import nl.mdb.nhb.nhclient.io.Location;
import nl.mdb.nhb.nhclient.io.Message;
import nl.mdb.nhb.nhclient.io.Order.Orders;

public interface NhClient {

	String getApiVersion();
	
	BuyInfo getBuyInfo();
	
	Orders getOrders(Location location, Algo algo);
	
	Orders getMyOrders(Location location, Algo algo);
	
	Message setPrice(Location location, Algo algo, Long orderId, BigDecimal price);
	
	Message decreasePrice(Location location, Algo algo, Long orderId);
}
