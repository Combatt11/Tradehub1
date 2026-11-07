package application.domain.services.order;

import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.*;
import application.domain.ports.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final OrderRepositoryPort orderRepository;
    private final ProductRepositoryPort productRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final BuyerRepositoryPort buyerRepository;

    public CartService(OrderRepositoryPort orderRepository, ProductRepositoryPort productRepository,
                       InventoryRepositoryPort inventoryRepository, BuyerRepositoryPort buyerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.buyerRepository = buyerRepository;
    }

    @Transactional
    public Order getOrCreateCart(String buyerId) {
        return orderRepository.findActiveCartByBuyerId(buyerId)
                .orElseGet(() -> orderRepository.save(Order.createCart(buyerId)));
    }

    @Transactional
    public Order addToCart(String buyerId, String productId, int quantity, String warehouseId) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new UnauthorizedOperationException("Buyer not found"));
        if (!buyer.canPurchase()) throw new UnauthorizedOperationException("Buyer cannot purchase");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new UnauthorizedOperationException("Product not found"));
        if (!product.isAvailableForSale())
            throw new UnauthorizedOperationException("Product is not available for sale");

        if (product.isPhysical()) {
            InventoryItem inventory = inventoryRepository.findByProductAndWarehouse(productId, warehouseId)
                    .orElseThrow(() -> new InsufficientStockException(productId, quantity, 0));
            if (!inventory.hasAvailable(quantity))
                throw new InsufficientStockException(productId, quantity, inventory.getQuantityAvailable());
            inventory.reserve(quantity);
            inventoryRepository.save(inventory);
        }

        Order cart = getOrCreateCart(buyerId);
        cart.addItem(new OrderItem(productId, product.getName(), quantity, product.getPrice(), warehouseId));
        return orderRepository.save(cart);
    }

    @Transactional
    public Order submitOrder(String buyerId, String shippingAddress) {
        Order cart = orderRepository.findActiveCartByBuyerId(buyerId)
                .orElseThrow(() -> new UnauthorizedOperationException("No active cart found"));
        cart.submitForPayment(shippingAddress);
        return orderRepository.save(cart);
    }
}
