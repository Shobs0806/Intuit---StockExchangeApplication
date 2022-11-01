package entities;

import entities.enums.OrderType;
import entities.enums.StockType;

public class Order {
    private String id;
    private long timeStamp;
    private StockType stockName;
    private OrderType action;
    private int quantity; // validation
    private double price; // price

    public Order(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public StockType getStockName() {
        return stockName;
    }

    public OrderType getAction() {
        return action;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setStockName(StockType stockName) {
        this.stockName = stockName;
    }

    public void setAction(OrderType action) {
        this.action = action;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
