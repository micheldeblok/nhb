package nl.mdb.nhb.order;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.mdb.nhb.NhbConfiguration;
import nl.mdb.nhb.spclient.SpClient;
import nl.mdb.nhb.spclient.SpStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import nl.mdb.nhb.nhclient.io.Message;
import nl.mdb.nhb.nhclient.io.Order;

@Slf4j
@Component
public class CheckOrdersJob implements Runnable {

	@Autowired
	private NhbConfiguration config;

	@Autowired
	private OrderModel model;

	@Autowired
	private SpClient spClient;

	@Scheduled(fixedRateString="${nhb.checkJobDelay}", initialDelayString="${nhb.checkJobInit}")
	public void run() {
		if (config.isCheckOrdersEnabled()) {
			checkOrders();
		} else {
			log.info("CheckOrdersJob is disabled");
		}
	}

	private void checkOrders() {
		model.initialize();
		log.info("++++++++++++++++++++++++++++++++++++++++");
		OrderStatistics s = model.getStatistics();
		log.info(s.toString());
		log.info("NH Balance: {}, SP Round Duration: {}", model.getBalance(), model.getRunningMinutes());
		log.info("My Orders:");
		for (Order o: model.getMyOrders()) {
			checkPriceTooLow(s.getLowestWorkingPrice(), o);
			checkSpeedTooHigh(o);
			checkForRefill(model.getMyOrders().size(), o);
		}
	}

	private void checkForRefill(int size, Order o) {
		if (size > 1) {
			log.info("More than 1 order active, so not checking for refill..");
			return;
		}
		BigDecimal threshold = config.getRefillBalanceThreshold();
		if (threshold == null) {
			log.debug("  Order refill is disabled..");
		} else if (model.getBalance().compareTo(threshold) < 0) {
			log.debug("  Balance {} is below refill threshold: {}", model.getBalance(), threshold);
		} else {
			log.info("  Balance {} is greater than refill threshold: {}, performing refill!", model.getBalance(), threshold);
			Message m = model.refill(o.getId(), model.getBalance());
			logMessage(m);
		}
	}

	private void checkPriceTooLow(BigDecimal lowest, Order o) {
		if (lowest == null || BigDecimal.ZERO.equals(lowest)) {
			log.warn("No lowestWorkingPrice available..");
			return;
		}

		if (model.isRunningIdle()) {
			log.info("- Order #{} is running idle, no need to raise price: {}", o.getId(), o.getPrice());
			return;
		}

		BigDecimal goodPrice = lowest.add(config.getPriceMargin());
		BigDecimal efficiency = o.getAccepted_speed().divide(o.getLimit_speed(), 2, RoundingMode.HALF_UP);
		if (o.getPrice().compareTo(goodPrice) < 0
		        || (o.getWorkers() == null || o.getWorkers().intValue() == 0)
                || efficiency.compareTo(new BigDecimal("0.20")) < 0) {

			// Something going on..
			if (goodPrice.compareTo(o.getPrice()) <= 0) {
				log.info("- Order #{} with price {} is higher than good price {}, just wait for more hash..",
					o.getId(), o.getPrice(), goodPrice);
			} else if (config.getMaxPrice() != null && goodPrice.compareTo(config.getMaxPrice()) > 0) {
				log.warn("- Order #{} with price={} is too low, setting maximum price: {}", o.getId(), config.getMaxPrice());
				Message m = model.setPrice(o.getId(), config.getMaxPrice());
				logMessage(m);
			} else {
				log.warn("- Order #{} with price={} is too low, raising price to: {}", o.getId(), o.getPrice(), goodPrice);
				Message m = model.setPrice(o.getId(), goodPrice);
				logMessage(m);
			}
			return;
		}
		log.info("- Order #{} with price={} is OK!", o.getId(), o.getPrice());
	}

	private void checkSpeedTooHigh(Order o) {
		if (model.getRunningMinutes() == null || model.getRunningMinutes().intValue() <= 0) {
			log.warn("No runningMinutes available..");
			return;
		}

		int numMinutes = model.getRunningMinutes().intValue();
		BigDecimal speed = config.getSpeedMax();
		if (numMinutes > 45) {
			speed = config.getSpeedMin();
		} else if (numMinutes > 30) {
			speed = speed.multiply(new BigDecimal("0.15"), new MathContext(2, RoundingMode.HALF_UP));
		} else if (numMinutes > 15) {
			speed = speed.multiply(new BigDecimal("0.40"), new MathContext(2, RoundingMode.HALF_UP));
		}
		if (speed.compareTo(config.getSpeedMin()) < 0) {
			speed = config.getSpeedMin();
		}
		speed = speed.setScale(2, RoundingMode.HALF_UP);
		// Check speed
		if (o.getLimit_speed().compareTo(speed) < 0) {
			log.info("  Speed is {} which is too low, increasing it to: {}", o.getLimit_speed(), speed);
			Message m = model.setLimit(o.getId(), speed);
			logMessage(m);
		} else if (o.getLimit_speed().compareTo(speed) == 0) {
			log.info("  Speed equals the required speed of: {}", o.getLimit_speed(), speed);
		} else {
			log.info("  Speed is {} which is too high, lowering it to: {}", o.getLimit_speed(), speed);
			Message m = model.setLimit(o.getId(), speed);
			logMessage(m);
		}
	}

	private void logMessage(Message m) {
		if (m.getSuccess() != null) {
			log.info("  success: " + m.getSuccess());
		} else {
			log.error("  error: " + m.getError());
		}
	}
}
