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
class TradingSystem {
    private final Map<String, Queue<PricePoint>> priceHistory;
    private final Map<String, TradingStrategy> strategies;
    private final int maxHistorySize;
    private final Map<String, Position> positions;

    public TradingSystem(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
        this.priceHistory = new HashMap<>();
        this.strategies = new HashMap<>();
        this.positions = new HashMap<>();
    }

    public void addPricePoint(PricePoint pricePoint) {
        String symbol = pricePoint.getSymbol();
        priceHistory.computeIfAbsent(symbol, k -> new LinkedBlockingQueue<>());
        
        Queue<PricePoint> symbolHistory = priceHistory.get(symbol);
        symbolHistory.add(pricePoint);
        
        while (symbolHistory.size() > maxHistorySize) {
            symbolHistory.poll();
        }
        
        analyzeTrades(symbol);
    }

    public void setStrategy(String symbol, TradingStrategy strategy) {
        strategies.put(symbol, strategy);
    }

    private void analyzeTrades(String symbol) {
        TradingStrategy strategy = strategies.get(symbol);
        if (strategy == null) return;

        Queue<PricePoint> symbolHistory = priceHistory.get(symbol);
        Position currentPosition = positions.getOrDefault(symbol, new Position(symbol));

        if (currentPosition.getQuantity() == 0 && strategy.shouldBuy(symbolHistory)) {
            executeBuy(symbol, calculatePositionSize(symbol));
        } else if (currentPosition.getQuantity() > 0 && strategy.shouldSell(symbolHistory)) {
            executeSell(symbol, currentPosition.getQuantity());
        }
    }

    private void executeBuy(String symbol, int quantity) {
        PricePoint latestPrice = getLatestPrice(symbol);
        if (latestPrice == null) return;

        Position position = positions.getOrDefault(symbol, new Position(symbol));
        position.buy(quantity, latestPrice.getPrice());
        positions.put(symbol, position);
        
        System.out.printf("Executed BUY: %s, Quantity: %d, Price: %.2f%n", 
            symbol, quantity, latestPrice.getPrice());
    }

    private void executeSell(String symbol, int quantity) {
        PricePoint latestPrice = getLatestPrice(symbol);
        if (latestPrice == null) return;

        Position position = positions.get(symbol);
        if (position != null) {
            position.sell(quantity, latestPrice.getPrice());
            System.out.printf("Executed SELL: %s, Quantity: %d, Price: %.2f%n", 
                symbol, quantity, latestPrice.getPrice());
        }
    }

    private PricePoint getLatestPrice(String symbol) {
        Queue<PricePoint> symbolHistory = priceHistory.get(symbol);
        return symbolHistory != null && !symbolHistory.isEmpty() ? 
            ((LinkedList<PricePoint>)symbolHistory).getLast() : null;
    }

    private int calculatePositionSize(String symbol) {
        return 100;
    }
}
class Position {
    private final String symbol;
    private int quantity;
    private double averagePrice;
    private double totalPnL;

    public Position(String symbol) {
        this.symbol = symbol;
        this.quantity = 0;
        this.averagePrice = 0.0;
        this.totalPnL = 0.0;
    }
    
      public void buy(int quantity, double price) {
        if (this.quantity > 0) {
            this.averagePrice = ((this.averagePrice * this.quantity) + (price * quantity)) 
                / (this.quantity + quantity);
        } else {
            this.averagePrice = price;
        }
        this.quantity += quantity;
    }

    public void sell(int quantity, double price) {
        if (quantity > this.quantity) {
            quantity = this.quantity;
        }
         double pnL = (price - averagePrice) * quantity;
        totalPnL += pnL;
        this.quantity -= quantity;
        
        if (this.quantity == 0) {
            this.averagePrice = 0.0;
        }
    }

    public int getQuantity() { return quantity; }
    public double getAveragePrice() { return averagePrice; }
    public double getTotalPnL() { return totalPnL; }
}
