package application.infrastructure.seed;

import application.domain.models.*;
import application.domain.ports.out.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepositoryPort userRepository;
    private final BuyerRepositoryPort buyerRepository;
    private final SellerRepositoryPort sellerRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final ProductRepositoryPort productRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepositoryPort userRepository, BuyerRepositoryPort buyerRepository,
                          SellerRepositoryPort sellerRepository, WarehouseRepositoryPort warehouseRepository,
                          ProductRepositoryPort productRepository, InventoryRepositoryPort inventoryRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.buyerRepository = buyerRepository;
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@tradehub.com").isPresent()) return;

        User admin = User.create("System Admin", "admin@tradehub.com",
                passwordEncoder.encode("admin123"), User.Role.ADMINISTRATOR, "ADMIN-001");
        userRepository.save(admin);

        User buyerUser = User.create("John Buyer", "buyer@example.com",
                passwordEncoder.encode("buyer123"), User.Role.BUYER, "BUY-001");
        buyerUser = userRepository.save(buyerUser);
        buyerRepository.save(Buyer.create(buyerUser.getId(), "123 Main Street, City"));

        User sellerUser = User.create("Alice Seller", "seller@example.com",
                passwordEncoder.encode("seller123"), User.Role.SELLER, "SEL-001");
        sellerUser = userRepository.save(sellerUser);
        Seller seller = Seller.create(sellerUser.getId(), "Alice Electronics", "TAX-999", "+123456789");
        seller.approve();
        seller = sellerRepository.save(seller);

        Warehouse warehouse = Warehouse.createForSeller("Alice Main Warehouse", "456 Storage Ave", seller.getId());
        warehouse = warehouseRepository.save(warehouse);

        Product laptop = Product.create(seller.getId(), "Laptop Pro 15", "High-end laptop",
                Product.ProductType.PHYSICAL, new BigDecimal("1299.99"), "LAP-001");
        laptop.publish();
        laptop = productRepository.save(laptop);

        Product ebook = Product.create(seller.getId(), "Java Guide eBook", "Complete Java guide",
                Product.ProductType.DIGITAL, new BigDecimal("29.99"), "EBK-001");
        ebook.publish();
        productRepository.save(ebook);

        inventoryRepository.save(InventoryItem.create(laptop.getId(), warehouse.getId(), 50));

        User logistics = User.create("Bob Logistics", "logistics@example.com",
                passwordEncoder.encode("logistics123"), User.Role.LOGISTICS_OPERATOR, "LOG-001");
        userRepository.save(logistics);

        System.out.println("=== TradeHub seed data loaded ===");
        System.out.println("Admin:     admin@tradehub.com / admin123");
        System.out.println("Buyer:     buyer@example.com / buyer123");
        System.out.println("Seller:    seller@example.com / seller123");
        System.out.println("Logistics: logistics@example.com / logistics123");
    }
}
