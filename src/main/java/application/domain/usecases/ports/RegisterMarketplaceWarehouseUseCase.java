package application.domain.usecases.ports;

import application.domain.entities.Warehouse;

public interface RegisterMarketplaceWarehouseUseCase {
  Warehouse registerMarketplaceWarehouse(String name, String address);
}
