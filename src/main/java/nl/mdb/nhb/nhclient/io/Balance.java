package nl.mdb.nhb.nhclient.io;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Balance {

    private BigDecimal balance_confirmed;

    private BigDecimal balance_pending;

    public static class BalanceResponse extends AbstractResponse<Balance> {}
}
