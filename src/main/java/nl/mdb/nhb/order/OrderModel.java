package nl.mdb.nhb.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import nl.mdb.nhb.spclient.SpClient;
import nl.mdb.nhb.spclient.SpStatistics;
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

	private Pattern DURATION = Pattern.compile("(\\d\\d):(\\d\\d):(\\d\\d)");

	@Autowired
	private NhbConfiguration config;
	
	@Autowired
	private NhClient client;

	@Autowired
	private SpClient spClient;
	
	@Getter
	private BigDecimal maxPriceDown;
	
	@Getter
	private List<Order> orders = new ArrayList<>();
	
	@Getter
	private List<Order> myOrders = new ArrayList<>();

	@Getter
	private BigDecimal balance;

	@Getter
	private Integer runningMinutes;
	
	@SneakyThrows
	public void initialize() {
		log.debug("Initializing OrderModel..");
		loadMaxPriceDown();
		loadOrders();
		loadMyOrders();
		loadBalance();
		loadRunningMinutes();
	}

	private void loadBalance() {
		this.balance = client.getBalance().getBalance_confirmed();
	}

	private void loadRunningMinutes() {
		SpStatistics stats = spClient.getStatistics();
		Matcher matcher = DURATION.matcher(stats.getRound_duration());
		if (matcher.matches() && matcher.groupCount() == 3) {
			this.runningMinutes = (Integer.parseInt(matcher.group(1)) * 60) + Integer.parseInt(matcher.group(2));
		} else {
			log.warn("Unable to parse duration: {}", stats.getRound_duration());
		}
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
		result.setMaxPriceDown(this.maxPriceDown);
		return result;
	}

	public Message setLimit(Long orderId, BigDecimal limit) {
		return client.setLimit(getLocation(), getAlgo(), orderId, limit);
	}

	public Message setPrice(Long orderId, BigDecimal price) {
		return client.setPrice(getLocation(), getAlgo(), orderId, price);
	}

	public Message decreasePrice(Long orderId) {
		return client.decreasePrice(getLocation(), getAlgo(), orderId);
	}

	public Message refill(Long orderId, BigDecimal amount) {
		return client.refill(getLocation(), getAlgo(), orderId, amount);
	}

	public Integer getRunningMinutes() {
		return this.runningMinutes;
	}

	public boolean isRunningIdle() {
		return config.getRunningIdleMinutes() != null && this.runningMinutes != null
				&& this.runningMinutes > config.getRunningIdleMinutes();
	}

	private void loadMaxPriceDown() {
		if (config.getMaxPriceDown() !=  null && config.getMaxPriceDown().compareTo(BigDecimal.ZERO) >= 0) {
			this.maxPriceDown = config.getMaxPriceDown();
			return;
		}
		// Not specified? Then obtain from API call:
		BuyInfo bi = client.getBuyInfo();
		Optional<Algorithm> oa = Arrays.asList(bi.getAlgorithms()).stream()
			.filter(a -> a.getAlgo() == getAlgo().getCode())
			.findFirst();
		if (!oa.isPresent()) {
			throw new RuntimeException("No Algorithm found in buy info for: " + getAlgo());
		}
		this.maxPriceDown = oa.get().getDown_step().abs();
	}
	
	private void loadOrders() {
		Orders o = client.getOrders(getLocation(), getAlgo());
		orders.clear();
		orders.addAll(Arrays.asList(o.getOrders()));
		// Order by price descending
		orders.sort((o1, o2) -> {
			return o1.getPrice().compareTo(o2.getPrice());
		});
	}
	
	private void loadMyOrders() {
		Orders o = client.getMyOrders(getLocation(), getAlgo());
		myOrders.clear();
		myOrders.addAll(Arrays.asList(o.getOrders()));
	}

	private Algo getAlgo() {
		return Algo.valueOf(config.getAlgo());
	}

	private Location getLocation() {
		return Location.valueOf(config.getLocation());
	}
}
