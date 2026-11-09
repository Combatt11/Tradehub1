package application.domain.usecases;

import application.domain.entities.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.usecases.ports.RegisterMarketplaceWarehouseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterMarketplaceWarehouseService implements RegisterMarketplaceWarehouseUseCase {

  private final WarehouseRepositoryPort warehouseRepository;

  public RegisterMarketplaceWarehouseService(WarehouseRepositoryPort warehouseRepository) {
    this.warehouseRepository = warehouseRepository;
  }

  @Override
  @Transactional
  public Warehouse registerMarketplaceWarehouse(String name, String address) {
    return warehouseRepository.save(Warehouse.createMarketplace(name, address));
  }
}
