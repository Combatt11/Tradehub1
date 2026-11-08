package application.domain.ports.in;

import application.domain.models.Warehouse;

public interface RegisterMarketplaceWarehouseUseCase {
  Warehouse registerMarketplaceWarehouse(String name, String address);
}
