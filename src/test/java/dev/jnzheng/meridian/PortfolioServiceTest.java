package dev.jnzheng.meridian;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PortfolioServiceTest {

    @Test
    void volatilityMatchesSampleStdDev() {
        // Prices 10, 20, 30. returns 1.0 ((20 - 10) / 10) and 0.5 ((30 - 20) / 20).
        // sample stddev becomes 0.353553 (rounded 6 dec places). Take mean (1.5 / 2) = 0.75
        // sum of squared diffs = 0.125 (returns - mean)^2, sample variance = 0.125/(2-1) = 0.125
        // finally, sqrt(0.125) = 0.353553...
        List<BigDecimal> prices = List.of(
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("30")
        );

        BigDecimal result = PortfolioService.volatility(prices);

        assertEquals(new BigDecimal("0.353553"), result);
    }

    @Test
    void volatilityReturnsNull() {
        assertNull(PortfolioService.volatility(null)); //if no daily prices, should be null
        assertNull(PortfolioService.volatility(List.of())); //returns < 2, nothing to compare
        assertNull(PortfolioService.volatility(List.of(new BigDecimal("10")))); //same as above
        assertNull(PortfolioService.volatility(List.of( //two prices makes one return, so returns.size() is still < 2, null.
                new BigDecimal("10"),
                new BigDecimal("20")
        )));
    }
}
