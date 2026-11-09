package application.domain.usecases;

import application.domain.entities.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.usecases.ports.RegisterSellerWarehouseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterSellerWarehouseService implements RegisterSellerWarehouseUseCase {

  private final WarehouseRepositoryPort warehouseRepository;

  public RegisterSellerWarehouseService(WarehouseRepositoryPort warehouseRepository) {
    this.warehouseRepository = warehouseRepository;
  }

  @Override
  @Transactional
  public Warehouse registerSellerWarehouse(String name, String address, String sellerId) {
    return warehouseRepository.save(Warehouse.createForSeller(name, address, sellerId));
  }
}
