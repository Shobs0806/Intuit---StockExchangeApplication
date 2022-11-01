package service;

import entities.Order;
import entities.Trade;
import entities.enums.OrderType;
import entities.enums.StockType;
import queues.BuyerOrderQueue;
import queues.SellerOrderQueue;
import queues.TradeBook;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StockExchangeService {

        private final Map<StockType, BuyerOrderQueue> buyerOrderBook;
        private final Map<StockType, SellerOrderQueue> sellerOrderBook;
        private final TradeBook tradeBook;

        public StockExchangeService() {
            buyerOrderBook = new ConcurrentHashMap<>();
            sellerOrderBook = new ConcurrentHashMap<>();
            tradeBook = new TradeBook();
        }

        public List<Trade> processTrade(List<Order> orders) {
            List<Trade> trades = null;
            if (orders != null && !orders.isEmpty()) {
                orders = orders.stream().sorted(Comparator.comparing(Order::getTimeStamp)).collect(Collectors.toList());
                orders.forEach(this::processingOrders);
                trades = tradeBook.getAllTrades();
            }
            return trades;
        }

        private void processingOrders(Order order) {
            StockType stock = order.getStockName();
            BuyerOrderQueue buyerOrderQueue = buyerOrderBook.getOrDefault(stock, new BuyerOrderQueue());
            SellerOrderQueue sellerOrderQueue = sellerOrderBook.getOrDefault(stock, new SellerOrderQueue());
            if (OrderType.BUY.equals(order.getAction()))
                buyerOrderQueue.add(order);
            else
                sellerOrderQueue.add(order);
            processTradeTransaction(buyerOrderQueue, sellerOrderQueue);
            buyerOrderBook.put(stock, buyerOrderQueue);
            sellerOrderBook.put(stock, sellerOrderQueue);
        }

        private void processTradeTransaction(BuyerOrderQueue buyerOrderQueue, SellerOrderQueue sellerOrderQueue) {
            while (ordersMatchExecution(buyerOrderQueue.getBestOrder(), sellerOrderQueue.getBestOrder())) {
                Order buyOrder = buyerOrderQueue.removeBestOrder();
                Order sellOrder = sellerOrderQueue.removeBestOrder();
                if (buyOrder.getQuantity() >= sellOrder.getQuantity()) {
                    tradeBook.addTrade(new Trade(sellOrder.getId(), sellOrder.getQuantity(), sellOrder.getPrice(), buyOrder.getId()));
                    buyOrder.setQuantity(buyOrder.getQuantity() - sellOrder.getQuantity());
                    if (buyOrder.getQuantity() > 0)
                        buyerOrderQueue.add(buyOrder);
                } else {
                    tradeBook.addTrade(new Trade(sellOrder.getId(), buyOrder.getQuantity(), sellOrder.getPrice(), buyOrder.getId()));
                    sellOrder.setQuantity(sellOrder.getQuantity() - buyOrder.getQuantity());
                    sellerOrderQueue.add(sellOrder);
                }
            }
        }

        private boolean ordersMatchExecution(Order buyOrder, Order sellOrder) {
            return  buyOrder != null &&
                    sellOrder != null &&
                    (buyOrder.getPrice() >= sellOrder.getPrice());
        }
}
