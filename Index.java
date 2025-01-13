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
