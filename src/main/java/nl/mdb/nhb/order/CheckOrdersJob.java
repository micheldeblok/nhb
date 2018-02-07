package nl.mdb.nhb.order;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import nl.mdb.nhb.NhbConfiguration;
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

	@Scheduled(fixedRateString="${nhb.checkJobDelay}", initialDelayString="${nhb.checkJobInit}")
	public void run() {
		model.initialize();
		log.info("++++++++++++++++++++++++++++++++++++++++");
		OrderStatistics s = model.getStatistics();
		log.info(s.toString());
		log.info("My Orders:");
		for (Order o: model.getMyOrders()) {
			checkPriceTooLow(s.getLowestWorkingPrice(), o);
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

		BigDecimal goodPrice = lowest.add(config.getPriceMargin());
		BigDecimal efficiency = o.getAccepted_speed().divide(o.getLimit_speed(), 2, RoundingMode.HALF_UP);
		if (o.getPrice().compareTo(goodPrice) < 0
		        || (o.getWorkers() == null || o.getWorkers().intValue() == 0)
                || efficiency.compareTo(new BigDecimal("0.60")) < 0) {
			if (config.getMaxPrice() != null && goodPrice.compareTo(config.getMaxPrice()) > 0) {
				log.warn("- Order #{} with price={} is too low, but has reached maximum price!", o.getId(), o.getPrice());
			} else {
				log.warn("- Order #{} with price={} is too low, raising price to: {}", o.getId(), o.getPrice(), goodPrice);
				Message m = model.setPrice(o.getId(), goodPrice);
				logMessage(m);
			}
			return;
		}
		log.info("- Order #{} with price={} is OK!", o.getId(), o.getPrice());
	}

	private void logMessage(Message m) {
		if (m.getSuccess() != null) {
			log.info("  success: " + m.getSuccess());
		} else {
			log.error("  error: " + m.getError());
		}
	}
}
