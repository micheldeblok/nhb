package nl.mdb.nhb.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.mdb.nhb.NhbConfiguration;
import nl.mdb.nhb.nhclient.NhClient;
import nl.mdb.nhb.nhclient.io.Algo;
import nl.mdb.nhb.nhclient.io.Algorithm;
import nl.mdb.nhb.nhclient.io.BuyInfo;
import nl.mdb.nhb.nhclient.io.Location;
import nl.mdb.nhb.nhclient.io.Message;
import nl.mdb.nhb.nhclient.io.Order;
import nl.mdb.nhb.nhclient.io.Order.Orders;
import nl.mdb.nhb.nhclient.io.OrderType;

@Slf4j
@Component
public class OrderModel {

	@Autowired
	private NhbConfiguration config;
	
	@Autowired
	private NhClient client;
	
	@Getter
	private Algorithm algorithm;
	
	@Getter
	private List<Order> orders = new ArrayList<>();
	
	@Getter
	private List<Order> myOrders = new ArrayList<>();
	
	@SneakyThrows
	public void initialize() {
		log.debug("Initializing OrderModel..");
		loadAlgorithm();
		loadOrders();
		loadMyOrders();
	}

	public OrderStatistics getStatistics() {

		// Get and sort all working orders:
		List<Order> workingOrders = orders.stream().filter(o -> {
			// Filter alive, standard only and with workers..
			return (Boolean.TRUE.equals(o.getAlive()) && o.getWorkers() != null && o.getWorkers() > 0 
				&& o.getType() != null && o.getType().intValue() == OrderType.STANDARD.getCode()); 
		}).collect(Collectors.toList());
		
		// Create statistics
		OrderStatistics result = new OrderStatistics();
		result.setNumberOfOrders(orders.size());
		result.setNumberOfWorkingOrders(workingOrders.size());
		result.setLowestWorkingPrice(workingOrders.get(0).getPrice());
		result.setHighestWorkingPrice(workingOrders.get(workingOrders.size() - 1).getPrice());
		result.setMaxPriceDown(algorithm.getDown_step().abs());
		return result;
	}

	public Message setPrice(Long orderId, BigDecimal price) {
		return client.setPrice(getLocation(), getAlgo(), orderId, price);
	}

	public Message decreasePrice(Long orderId) {
		return client.decreasePrice(getLocation(), getAlgo(), orderId);
	}

	private void loadAlgorithm() {
		BuyInfo bi = client.getBuyInfo();
		Optional<Algorithm> oa = Arrays.asList(bi.getAlgorithms()).stream()
			.filter(a -> a.getAlgo() == getAlgo().getCode())
			.findFirst();
		if (!oa.isPresent()) {
			throw new RuntimeException("No Algorithm found in buy info for: " + getAlgo());
		}
		this.algorithm = oa.get();
		// Stick to SLA
		sleep(3);
	}
	
	private void loadOrders() {
		Orders o = client.getOrders(getLocation(), getAlgo());
		orders.clear();
		orders.addAll(Arrays.asList(o.getOrders()));
		// Order by price descending
		orders.sort((o1, o2) -> {
			return o1.getPrice().compareTo(o2.getPrice());
		});
		// Stick to SLA
		sleep(3);
	}
	
	private void loadMyOrders() {
		Orders o = client.getMyOrders(getLocation(), getAlgo());
		myOrders.clear();
		myOrders.addAll(Arrays.asList(o.getOrders()));
		// Stick to SLA
		sleep(3);
	}

	private Algo getAlgo() {
		return Algo.valueOf(config.getAlgo());
	}

	private Location getLocation() {
		return Location.valueOf(config.getLocation());
	}

	@SneakyThrows
	private void sleep(int seconds) {
		Thread.sleep(seconds * 1000);
	}
}
