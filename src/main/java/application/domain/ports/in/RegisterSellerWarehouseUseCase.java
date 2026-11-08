package application.domain.ports.in;

import application.domain.models.Warehouse;

public interface RegisterSellerWarehouseUseCase {
  Warehouse registerSellerWarehouse(String name, String address, String sellerId);
}
