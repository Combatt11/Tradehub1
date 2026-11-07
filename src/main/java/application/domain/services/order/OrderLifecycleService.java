package application.domain.services.order;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderLifecycleService {
    private final OrderRepositoryPort orderRepository;

    public OrderLifecycleService(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order confirmPayment(String orderId, String buyerId, String paymentReference) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new UnauthorizedOperationException("Order not found"));
        if (!order.getBuyerId().equals(buyerId))
            throw new UnauthorizedOperationException("Order does not belong to this buyer");
        order.confirmPayment(paymentReference);
        return orderRepository.save(order);
    }

    @Transactional
    public Order markPreparing(String orderId) {
        Order order = find(orderId);
        order.markPreparing();
        return orderRepository.save(order);
    }

    @Transactional
    public Order markDispatched(String orderId) {
        Order order = find(orderId);
        order.markDispatched();
        return orderRepository.save(order);
    }

    @Transactional
    public Order markDelivered(String orderId) {
        Order order = find(orderId);
        order.markDelivered();
        return orderRepository.save(order);
    }

    @Transactional
    public Order finalizeOrder(String orderId) {
        Order order = find(orderId);
        order.finalizeOrder();
        return orderRepository.save(order);
    }

    private Order find(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new UnauthorizedOperationException("Order not found"));
    }
}
