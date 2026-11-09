package application.domain.ports.out;

import application.domain.entities.Warehouse;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepositoryPort {
  Warehouse save(Warehouse warehouse);

  Optional<Warehouse> findById(String id);

  List<Warehouse> findBySellerId(String sellerId);

  List<Warehouse> findAll();
}
