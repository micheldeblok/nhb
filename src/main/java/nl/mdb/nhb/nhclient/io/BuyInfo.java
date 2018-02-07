package nl.mdb.nhb.nhclient.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BuyInfo {

	private Algorithm[] algorithms;
	private Long down_time;
	private BigDecimal static_fee;
	private BigDecimal min_amount;
	private BigDecimal dynamic_fee;
	
	public static class BuyInfoResponse extends AbstractResponse<BuyInfo> {}
}
