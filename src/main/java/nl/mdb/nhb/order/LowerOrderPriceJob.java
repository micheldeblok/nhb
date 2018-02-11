package nl.mdb.nhb.order;

import java.math.BigDecimal;

import nl.mdb.nhb.NhbConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import nl.mdb.nhb.nhclient.io.Message;
import nl.mdb.nhb.nhclient.io.Order;

@Slf4j
@Component
public class LowerOrderPriceJob implements Runnable {

	@Autowired
	private NhbConfiguration config;

	@Autowired
	private OrderModel model;

	@Scheduled(fixedRateString="${nhb.lowerJobDelay}", initialDelayString="${nhb.lowerJobInit}")
	public void run() {
		if (config.isLowerOrdersEnabled()) {
			lowerOrders();
		} else {
			log.info("LowerOrderPriceJob is disabled");
		}
	}

	private void lowerOrders() {
		model.initialize();
		log.info("----------------------------------------");
		OrderStatistics s = model.getStatistics();
		log.info(s.toString());
		log.info("NH Balance: {}, SP Round Duration: {}", model.getBalance(), model.getRunningMinutes());
		BigDecimal maxDown = s.getMaxPriceDown();
		BigDecimal lowest = s.getLowestWorkingPrice();

		if (maxDown == null || lowest == null) {
			log.warn("No maxPriceDown or lowestWorkingPrice available..");
			return;
		}

		log.info("My Orders:");
		BigDecimal goodPrice = lowest.add(config.getPriceMargin());
		for (Order o: model.getMyOrders()) {
			if (config.getMinPrice() != null && o.getPrice().compareTo(config.getMinPrice()) <= 0) {
				log.warn("- Order #{} with price {} has reached minimum price: {}",
						o.getId(), o.getPrice(), config.getMinPrice());
			} else if (model.isRunningIdle()) {
				log.info("- Order #{} is running idle so lowering price..", o.getId());
				Message m = model.decreasePrice(o.getId());
				logMessage(m);
			} else if (o.getPrice().compareTo(goodPrice) > 0) {
				log.warn("- Order #{} with price={} is too expensive: a good price is {} so lowering price..",
						o.getId(), o.getPrice(), goodPrice);
				if (o.getPrice().subtract(maxDown).compareTo(goodPrice) < 0) {
					BigDecimal tempPrice = goodPrice.add(maxDown);
					log.info("  First raising the price to {}", tempPrice);
					Message m = model.setPrice(o.getId(), tempPrice);
					logMessage(m);
					log.info("  Then lowering the price to {}", goodPrice);
				}
				Message m = model.decreasePrice(o.getId());
				logMessage(m);
			} else {
				log.info("- Order #{} with price={} is OK!", o.getId(), o.getPrice());
			}
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
