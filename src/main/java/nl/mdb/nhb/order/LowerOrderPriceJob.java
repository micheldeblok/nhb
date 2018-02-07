package nl.mdb.nhb.order;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import nl.mdb.nhb.nhclient.io.Order;

@Slf4j
@Component
public class LowerOrderPriceJob implements Runnable {

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
		for (Order o: model.getMyOrders()) {
			if (o.getPrice().subtract(maxDown).compareTo(lowest) > 0) {
				log.warn("Order #{} with price={} is too expensive: lowest is {} so lowering price..", o.getId(), o.getPrice(), lowest);
				log.info("result: " + model.decreasePrice(o.getId()));
			} else {
				log.info("Order #{} with price={} is OK!", o.getId(), o.getPrice());
			}
		}
	}
}
