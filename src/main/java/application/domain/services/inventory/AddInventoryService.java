package application.domain.services.inventory;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.InventoryItem;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddInventoryService {
    private final InventoryRepositoryPort inventoryRepository;
    private final ProductRepositoryPort productRepository;
    private final WarehouseRepositoryPort warehouseRepository;

    public AddInventoryService(InventoryRepositoryPort inventoryRepository,
                               ProductRepositoryPort productRepository,
                               WarehouseRepositoryPort warehouseRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public InventoryItem execute(String productId, String warehouseId, int quantity, String sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new UnauthorizedOperationException("Product not found"));
        if (!product.getSellerId().equals(sellerId))
            throw new UnauthorizedOperationException("Product does not belong to this seller");
        if (!product.isPhysical())
            throw new UnauthorizedOperationException("Digital products do not require inventory");
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new UnauthorizedOperationException("Warehouse not found"));
        if (!warehouse.isActive())
            throw new UnauthorizedOperationException("Warehouse is not active");

        return inventoryRepository.findByProductAndWarehouse(productId, warehouseId)
                .map(item -> {
                    item.addStock(quantity);
                    return inventoryRepository.save(item);
                })
                .orElseGet(() -> inventoryRepository.save(InventoryItem.create(productId, warehouseId, quantity)));
    }
}
