package application.domain.services.warehouse;

import application.domain.exceptions.SellerNotFoundException;
import application.domain.exceptions.SellerRequiredForWarehouseException;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.in.RegisterSellerWarehouseUseCase;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterSellerWarehouseService implements RegisterSellerWarehouseUseCase {

  private final WarehouseRepositoryPort warehouseRepository;
  private final SellerRepositoryPort sellerRepository;

  public RegisterSellerWarehouseService(
      WarehouseRepositoryPort warehouseRepository, SellerRepositoryPort sellerRepository) {
    this.warehouseRepository = warehouseRepository;
    this.sellerRepository = sellerRepository;
  }

  @Override
  @Transactional
  public Warehouse registerSellerWarehouse(String name, String address, String sellerId) {
    if (sellerId == null || sellerId.isBlank()) {
      throw new SellerRequiredForWarehouseException();
    }
    Seller seller =
        sellerRepository
            .findById(sellerId)
            .orElseThrow(() -> new SellerNotFoundException(sellerId));
    seller.ensureActive();
    return warehouseRepository.save(Warehouse.createForSeller(name, address, seller.getId()));
  }
}
