package nl.mdb.nhb.order;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import nl.mdb.nhb.nhclient.io.Order;

@Slf4j
@Component
public class PreventIdleOrdersJob implements Runnable {

	@Autowired
	private OrderModel model;

	@Scheduled(fixedRateString="${nhb.idleJobDelay}", initialDelayString="${nhb.idleJobInit}")
	public void run() {
		model.initialize();
		log.info("++++++++++++++++++++++++++++++++++++++++");
		OrderStatistics s = model.getStatistics();
		BigDecimal lowest = s.getLowestWorkingPrice();
		if (lowest == null) {
			log.warn("No lowestWorkingPrice available..");
			return;
		}
		log.info(s.toString());
		log.info("My Orders:");
		for (Order o: model.getMyOrders()) {
			if ((o.getWorkers() == null || o.getWorkers().intValue() == 0) && o.getPrice().compareTo(lowest) < 0) {
				BigDecimal newPrice = lowest.add(new BigDecimal("0.0001"));
				log.warn("- Order #{} with price={} is too low. Raising price to: {}", o.getId(), o.getPrice(), newPrice);
				log.info("  result: " + model.setPrice(o.getId(), newPrice));
			} else {
				log.info("- Order #{} with price={} is OK!", o.getId(), o.getPrice());
			}
		}
	}
}
