package application.domain.ports.in;
import application.domain.models.InventoryItem;
import application.domain.models.Product;
import java.math.BigDecimal;
import java.util.List;
public interface SellerPort {
    Product createProduct(String sellerId, String name, String description,
                          Product.ProductType type, BigDecimal price, String sku);
    Product publishProduct(String productId, String sellerId);
    InventoryItem addInventory(String productId, String warehouseId, int quantity, String sellerId);
    List<Product> listMyProducts(String sellerId);
}
