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
		model.initialize();
		log.info("----------------------------------------");
		OrderStatistics s = model.getStatistics();
		BigDecimal maxDown = s.getMaxPriceDown();
		BigDecimal lowest = s.getLowestWorkingPrice();
		if (maxDown == null || lowest == null) {
			log.warn("No maxPriceDown or lowestWorkingPrice available..");
			return;
		}

		BigDecimal goodPrice = lowest.add(config.getPriceMargin());
		for (Order o: model.getMyOrders()) {
			if (o.getPrice().subtract(maxDown).compareTo(goodPrice) > 0) {
				log.warn("Order #{} with price={} is too expensive: a good price is {} so lowering price..",
						o.getId(), o.getPrice(), goodPrice);
				Message m = model.decreasePrice(o.getId());
				if (m.getSuccess() != null) {
					log.info("- success: " + m.getSuccess());
				} else {
					log.error("- error: " + m.getError());
				}
			} else {
				log.info("Order #{} with price={} is OK!", o.getId(), o.getPrice());
			}
		}
	}
}
