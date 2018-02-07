package nl.mdb.nhb.nhclient.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Order {

	private Integer type;
	private Long id;
	private BigDecimal price;
	private Integer algo;
	private Boolean alive;
	private BigDecimal limit_speed;
	private Integer workers;
	private BigDecimal accepted_speed;
	
	@Data
	public static class Orders {
		private Order[] orders;
	}
	
	public static class OrdersResponse extends AbstractResponse<Orders> {}
}
