import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

class PricePoint {
    private final LocalDateTime timestamp;
    private final String symbol;
    private final double price;
    private final long volume;

    public PricePoint(LocalDateTime timestamp, String symbol, double price, long volume) {
        this.timestamp = timestamp;
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
    }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public long getVolume() { return volume; }
}
interface TradingStrategy {
    boolean shouldBuy(Queue<PricePoint> priceHistory);
    boolean shouldSell(Queue<PricePoint> priceHistory);
}

class MovingAverageCrossoverStrategy implements TradingStrategy {
    private final int shortTermPeriod;
    private final int longTermPeriod;

    public MovingAverageCrossoverStrategy(int shortTermPeriod, int longTermPeriod) {
        this.shortTermPeriod = shortTermPeriod;
        this.longTermPeriod = longTermPeriod;
    }
