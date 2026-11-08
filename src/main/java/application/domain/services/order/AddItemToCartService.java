package application.domain.services.order;

import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.InventoryItem;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Product;
import application.domain.ports.in.AddItemToCartUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddItemToCartService implements AddItemToCartUseCase {

  private final OrderRepositoryPort orderRepository;
  private final ProductRepositoryPort productRepository;
  private final InventoryRepositoryPort inventoryRepository;
  private final BuyerRepositoryPort buyerRepository;
  private final GetCartService getCartService;

  public AddItemToCartService(
      OrderRepositoryPort orderRepository,
      ProductRepositoryPort productRepository,
      InventoryRepositoryPort inventoryRepository,
      BuyerRepositoryPort buyerRepository,
      GetCartService getCartService) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.inventoryRepository = inventoryRepository;
    this.buyerRepository = buyerRepository;
    this.getCartService = getCartService;
  }

  @Override
  @Transactional
  public Order addItem(String buyerId, String productId, int quantity, String warehouseId) {
    Buyer buyer =
        buyerRepository
            .findById(buyerId)
            .orElseThrow(() -> new UnauthorizedOperationException("Buyer not found"));
    if (!buyer.canPurchase()) throw new UnauthorizedOperationException("Buyer cannot purchase");

    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new UnauthorizedOperationException("Product not found"));
    if (!product.isAvailableForSale())
      throw new UnauthorizedOperationException("Product is not available for sale");

    if (product.isPhysical()) {
      InventoryItem inventory =
          inventoryRepository
              .findByProductAndWarehouse(productId, warehouseId)
              .orElseThrow(() -> new InsufficientStockException(productId, quantity, 0));
      if (!inventory.hasAvailable(quantity))
        throw new InsufficientStockException(productId, quantity, inventory.getQuantityAvailable());
      inventory.reserve(quantity);
      inventoryRepository.save(inventory);
    }

    Order cart = getCartService.getOrCreateCart(buyerId);
    cart.addItem(
        new OrderItem(productId, product.getName(), quantity, product.getPrice(), warehouseId));
    return orderRepository.save(cart);
  }
}
