package nl.mdb.nhb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.math.BigDecimal;

@Data
@Configuration
@ConfigurationProperties(prefix="nhb")
public class NhbConfiguration {

	private String baseUrl;
	
	private Long apiId;
	
	private String apiKey;
	
	private String apiReadOnlyKey;
	
	private String location;
	
	private String algo;

	private BigDecimal priceMargin;

	private BigDecimal minPrice;

	private BigDecimal maxPrice;

	private BigDecimal maxPriceDown = new BigDecimal("0.0010");

	private BigDecimal refillBalanceThreshold;

	private boolean lowerOrdersEnabled = false;

	private boolean checkOrdersEnabled = false;

	private String spUrl;

	private BigDecimal speedMin;

	private BigDecimal speedMax;

	private Integer runningIdleMinutes = 90;
}
