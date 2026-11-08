package application.domain.services.inventory;

import application.domain.exceptions.ProductNotFoundException;
import application.domain.exceptions.WarehouseNotFoundException;
import application.domain.models.InventoryItem;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.ports.in.AddInventoryUseCase;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddInventoryService implements AddInventoryUseCase {
  private final InventoryRepositoryPort inventoryRepository;
  private final ProductRepositoryPort productRepository;
  private final WarehouseRepositoryPort warehouseRepository;

  public AddInventoryService(
      InventoryRepositoryPort inventoryRepository,
      ProductRepositoryPort productRepository,
      WarehouseRepositoryPort warehouseRepository) {
    this.inventoryRepository = inventoryRepository;
    this.productRepository = productRepository;
    this.warehouseRepository = warehouseRepository;
  }

  @Override
  @Transactional
  public InventoryItem execute(
      String productId, String warehouseId, int quantity, String sellerId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    product.ensureOwnedBy(sellerId);
    product.ensurePhysical();
    Warehouse warehouse =
        warehouseRepository
            .findById(warehouseId)
            .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
    warehouse.ensureActive();

    return inventoryRepository
        .findByProductAndWarehouse(productId, warehouseId)
        .map(
            item -> {
              item.addStock(quantity);
              return inventoryRepository.save(item);
            })
        .orElseGet(
            () -> inventoryRepository.save(InventoryItem.create(productId, warehouseId, quantity)));
  }
}
