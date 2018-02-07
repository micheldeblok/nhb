package nl.mdb.nhb.nhclient.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Algorithm {

	private BigDecimal down_step;
	private BigDecimal min_diff_working;
	private BigDecimal min_limit;
	private String speed_text;
	private BigDecimal min_diff_initial;
	private String name;
	private int algo;
	private BigDecimal multi;
		
}
