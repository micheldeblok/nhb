package nl.mdb.nhb.nhclient;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import nl.mdb.nhb.NhbConfiguration;
import nl.mdb.nhb.nhclient.io.AbstractResponse;
import nl.mdb.nhb.nhclient.io.Algo;
import nl.mdb.nhb.nhclient.io.ApiVersion.ApiVersionResponse;
import nl.mdb.nhb.nhclient.io.BuyInfo;
import nl.mdb.nhb.nhclient.io.BuyInfo.BuyInfoResponse;
import nl.mdb.nhb.nhclient.io.Location;
import nl.mdb.nhb.nhclient.io.Message;
import nl.mdb.nhb.nhclient.io.Message.MessageResponse;
import nl.mdb.nhb.nhclient.io.Order.Orders;
import nl.mdb.nhb.nhclient.io.Order.OrdersResponse;

@Component
class NhClientImpl implements NhClient {

	@Autowired
	private NhbConfiguration config;
	
	private RestTemplate restTemplate;
	
	public NhClientImpl(RestTemplateBuilder builder) {
		this.restTemplate = builder.additionalMessageConverters(new MessageConverter()).build();
	}
	
	@Override
	public String getApiVersion() {
		ResponseEntity<ApiVersionResponse> response = restTemplate.getForEntity(config.getBaseUrl(), ApiVersionResponse.class);
		handleFault("getApiVersion", response);
		return response.getBody().getResult().getApi_version() != null ? 
				response.getBody().getResult().getApi_version() : "unknown";
	}

	@Override
	public BuyInfo getBuyInfo() {
		String url = config.getBaseUrl() + "?method=buy.info";
	    ResponseEntity<BuyInfoResponse> response = restTemplate.getForEntity(url, BuyInfoResponse.class);
	    handleFault("getBuyInfo", response);
        return response.getBody().getResult();
	}
	
	@Override
	public Orders getOrders(Location location, Algo algo) {
		String url = config.getBaseUrl() + "?method=orders.get&location=" + location.getCode() + "&algo=" + algo.getCode();
	    ResponseEntity<OrdersResponse> response = restTemplate.getForEntity(url, OrdersResponse.class);
	    handleFault("getOrders", response);
	    return response.getBody().getResult();
	}

	@Override
	public Orders getMyOrders(Location location, Algo algo) {
		checkApiKeys();
		String url = config.getBaseUrl() + "?method=orders.get&my&id=" + config.getApiId()
			+ "&key=" + config.getApiReadOnlyKey() + "&location=" + location.getCode() + "&algo=" + algo.getCode();
	    ResponseEntity<OrdersResponse> response = restTemplate.getForEntity(url, OrdersResponse.class);
	    handleFault("getMyOrders", response);
	    return response.getBody().getResult();
	}

	@Override
	public Message setPrice(Location location, Algo algo, Long orderId, BigDecimal price) {
		checkApiKeys();
		String url = config.getBaseUrl() + "?method=orders.set.price&id=" + config.getApiId()
			+ "&key=" + config.getApiKey() + "&location=" + location.getCode() + "&algo=" + algo.getCode()
			+ "&order=" + orderId + "&price=" + price;
		ResponseEntity<MessageResponse> response = restTemplate.getForEntity(url, MessageResponse.class);
		handleFault("setPrice", response);
		return response.getBody().getResult();
	}
	
	@Override
	public Message decreasePrice(Location location, Algo algo, Long orderId) {
		checkApiKeys();
		String url = config.getBaseUrl() + "?method=orders.set.price.decrease&id=" + config.getApiId()
			+ "&key=" + config.getApiKey() + "&location=" + location.getCode() + "&algo=" + algo.getCode()
			+ "&order=" + orderId;
		ResponseEntity<MessageResponse> response = restTemplate.getForEntity(url, MessageResponse.class);
		handleFault("decreasePrice", response);
		return response.getBody().getResult();
	}

	private void checkApiKeys() {
		if (config == null || config.getApiId() == null || config.getApiKey() == null || config.getApiReadOnlyKey() == null) {
			throw new RuntimeException("Missing API id or keys");
		}
	}
	
	private void handleFault(String method, ResponseEntity<? extends AbstractResponse<?>> response) {
		if (response.getBody() == null || response.getBody().getResult() == null || response.getStatusCodeValue() != 200) {
			throw new RuntimeException("Error for " + method + " : " + response.getStatusCodeValue());
		}
	}
}
