package application.domain.services;

import application.domain.exceptions.BuyerNotFoundException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.ProductNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.InventoryItem;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Product;
import application.domain.ports.in.AddItemToCartUseCase;
import application.domain.ports.in.GetCartUseCase;
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
  private final GetCartUseCase getCartUseCase;

  public AddItemToCartService(
      OrderRepositoryPort orderRepository,
      ProductRepositoryPort productRepository,
      InventoryRepositoryPort inventoryRepository,
      BuyerRepositoryPort buyerRepository,
      GetCartUseCase getCartUseCase) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.inventoryRepository = inventoryRepository;
    this.buyerRepository = buyerRepository;
    this.getCartUseCase = getCartUseCase;
  }

  @Override
  @Transactional
  public Order addItem(String buyerId, String productId, int quantity, String warehouseId) {
    Buyer buyer =
        buyerRepository.findById(buyerId).orElseThrow(() -> new BuyerNotFoundException(buyerId));
    buyer.ensureCanPurchase();

    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    product.ensureAvailableForSale();

    if (product.isPhysical()) {
      InventoryItem inventory =
          inventoryRepository
              .findByProductAndWarehouse(productId, warehouseId)
              .orElseThrow(() -> new InsufficientStockException(productId, quantity, 0));
      inventory.reserve(quantity);
      inventoryRepository.save(inventory);
    }

    Order cart = getCartUseCase.getOrCreateCart(buyerId);
    cart.addItem(
        new OrderItem(productId, product.getName(), quantity, product.getPrice(), warehouseId));
    return orderRepository.save(cart);
  }
}
