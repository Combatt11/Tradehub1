package application.domain.services;

import application.domain.models.Warehouse;
import application.domain.ports.in.RegisterSellerWarehouseUseCase;
import application.domain.ports.out.WarehouseRepositoryPort;
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
