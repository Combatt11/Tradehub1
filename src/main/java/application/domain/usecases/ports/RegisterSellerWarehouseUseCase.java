package application.domain.usecases.ports;

import application.domain.entities.Warehouse;

public interface RegisterSellerWarehouseUseCase {
  Warehouse registerSellerWarehouse(String name, String address, String sellerId);
}
