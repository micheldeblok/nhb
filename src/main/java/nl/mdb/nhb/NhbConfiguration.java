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

	private BigDecimal maxPrice;

	private BigDecimal refillBalanceThreshold;
}
