package nl.mdb.nhb.order;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderStatistics {

	private long numberOfOrders;
	
	private long numberOfWorkingOrders;
	
	private BigDecimal lowestWorkingPrice;
	
	private BigDecimal highestWorkingPrice;
	
	private BigDecimal maxPriceDown;

	private BigDecimal balance;
}
