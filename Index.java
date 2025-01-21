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
    @Override
    public boolean shouldBuy(Queue<PricePoint> priceHistory) {
        if (priceHistory.size() < longTermPeriod) return false;
        
        double shortTermMA = calculateMA(priceHistory, shortTermPeriod);
        double longTermMA = calculateMA(priceHistory, longTermPeriod);
        
        return shortTermMA > longTermMA;
    }

    @Override
    public boolean shouldSell(Queue<PricePoint> priceHistory) {
        if (priceHistory.size() < longTermPeriod) return false;
        
        double shortTermMA = calculateMA(priceHistory, shortTermPeriod);
        double longTermMA = calculateMA(priceHistory, longTermPeriod);
        
        return shortTermMA < longTermMA;
    }
    private double calculateMA(Queue<PricePoint> priceHistory, int period) {
        List<PricePoint> recentPrices = new ArrayList<>(priceHistory);
        int startIndex = Math.max(0, recentPrices.size() - period);
        
        return recentPrices.subList(startIndex, recentPrices.size()).stream()
                .mapToDouble(PricePoint::getPrice)
                .average()
                .orElse(0.0);
    }
}
