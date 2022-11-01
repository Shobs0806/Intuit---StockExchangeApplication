package service;


import entities.Order;
import entities.Trade;
import entities.enums.OrderType;
import entities.enums.StockType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StockExchangeServiceTest {

    private StockExchangeService target;

    @BeforeEach
    void setUp() {
        target = new StockExchangeService();
    }

    private Order getOrder() {
        Order order = new Order("dummy_id");
        order.setStockName(StockType.BAC);
        order.setQuantity(100);
        order.setAction(OrderType.BUY);
        order.setPrice(100.0);
        order.setTimeStamp(new Date().getTime());
        return order;
    }

    @Test
    public void process_handlesNull() {
        try {
            List<Trade> response = target.processTrade(null);
            assertNull(response);
        } catch (Exception e) {
            fail("Exception to be handled");
        }
    }

    @Test
    public void process_singleOrder_noMatch() {

        Order order = getOrder();
        List<Trade> response = target.processTrade(Collections.singletonList(order));
        assertTrue(response.isEmpty());
    }

    @Test
    public void process_orders_match() {
        //Arrange
        Order buyOrder = getOrder();
        Order sellOrder = getOrder();
        sellOrder.setAction(OrderType.SELL);

        List<Trade> response = target.processTrade(Arrays.asList(buyOrder, sellOrder));


        assertEquals(1, response.size());
    }

    @Test
    public void process_orders_misMatch() {
        //Arrange
        Order buyOrder = getOrder();
        buyOrder.setStockName(StockType.TCS);
        Order sellOrder = getOrder();
        sellOrder.setAction(OrderType.SELL);

        List<Trade> response = target.processTrade(Arrays.asList(buyOrder, sellOrder));

        assertEquals(0, response.size());
    }

    @Test
    public void processOrderMatchcheaperSellOrderExecutes() {

        Order buyOrder = getOrder();

        Order sellOrder1 = getOrder();
        sellOrder1.setAction(OrderType.SELL);
        sellOrder1.setPrice(97.56);

        Order sellOrder2 = getOrder();
        sellOrder2.setAction(OrderType.SELL);
        sellOrder2.setPrice(93.57);


        List<Trade> response = target.processTrade(Arrays.asList(sellOrder1, sellOrder2, buyOrder));

        assertEquals(response.get(0).getSellingPrice(), 93.57);
    }

    @Test
    public void processOrderMatchExpensiveSellOrder() {
        //Arrange
        Order buyOrder = getOrder();

        Order sellOrder1 = getOrder();
        sellOrder1.setAction(OrderType.SELL);
        sellOrder1.setPrice(97.56);

        Order sellOrder2 = getOrder();
        sellOrder2.setAction(OrderType.SELL);
        sellOrder2.setPrice(93.57);


        List<Trade> response = target.processTrade(Arrays.asList(sellOrder1, buyOrder, sellOrder2));


        assertEquals(response.get(0).getSellingPrice(), 97.56);
    }

    @Test
    public void processOrderMatchExpensiveBuyOrderGetsPriority() {
        //Arrange
        Order sellOrder = getOrder();
        sellOrder.setAction(OrderType.SELL);

        Order buyOrder1 = getOrder();
        buyOrder1.setQuantity(70);
        buyOrder1.setPrice(101.56);
        buyOrder1.setId("b1");

        Order buyOrder2 = getOrder();
        buyOrder2.setQuantity(70);
        buyOrder2.setPrice(103.57);
        buyOrder2.setId("b2");


        List<Trade> response = target.processTrade(Arrays.asList(buyOrder1, buyOrder2, sellOrder));


        assertEquals(response.get(0).getBuyOrderID(), "b2");
        assertEquals(response.get(0).getQuantity(), 70);
        assertEquals(response.get(1).getBuyOrderID(), "b1");
        assertEquals(response.get(1).getQuantity(), 30);
    }

    @Test
    public void processOrderMatchexpensiveBuyOrderGetsLessPriorityBasedOnOrder() {
        //Arrange
        Order sellOrder = getOrder();
        sellOrder.setAction(OrderType.SELL);
        sellOrder.setTimeStamp(1);

        Order buyOrder1 = getOrder();
        buyOrder1.setQuantity(70);
        buyOrder1.setPrice(101.56);
        buyOrder1.setId("b1");
        buyOrder1.setTimeStamp(1);

        Order buyOrder2 = getOrder();
        buyOrder2.setQuantity(70);
        buyOrder2.setPrice(103.57);
        buyOrder2.setId("b2");
        buyOrder2.setTimeStamp(2);


        List<Trade> response = target.processTrade(Arrays.asList(buyOrder1, buyOrder2, sellOrder));


        assertEquals(response.get(0).getBuyOrderID(), "b1");
        assertEquals(response.get(0).getQuantity(), 70);
        assertEquals(response.get(1).getBuyOrderID(), "b2");
        assertEquals(response.get(1).getQuantity(), 30);
    }
}