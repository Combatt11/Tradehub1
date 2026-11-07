package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaProductRepository extends JpaRepository<ProductJpaEntity, String> {
  List<ProductJpaEntity> findBySellerId(String sellerId);

  List<ProductJpaEntity> findByStatus(String status);
}
