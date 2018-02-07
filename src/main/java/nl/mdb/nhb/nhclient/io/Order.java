package nl.mdb.nhb.nhclient.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Order {

	private Integer type;
	private Long id;
	private BigDecimal price;
	private BigDecimal btc_avail;
	private BigDecimal btc_paid;
	private Integer algo;
	private Boolean alive;
	private BigDecimal limit_speed;
	private Integer workers;
	private BigDecimal accepted_speed;
	private Long end;
	
	@Data
	public static class Orders {
		private Order[] orders;
	}
	
	public static class OrdersResponse extends AbstractResponse<Orders> {}
}
