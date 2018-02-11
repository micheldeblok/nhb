package nl.mdb.nhb.spclient;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpStatistics {

//    private Integer active_workers;
    private String round_started;
    private String round_duration;
    private BigDecimal shares_cdf;
}
