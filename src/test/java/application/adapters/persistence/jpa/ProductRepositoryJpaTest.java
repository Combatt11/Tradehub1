package application.adapters.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.domain.entities.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import({ProductJpaMapper.class, ProductJpaAdapter.class})
@DisplayName("ProductJpaAdapter persistence")
class ProductRepositoryJpaTest {

  @Autowired private SpringDataJpaProductRepository repository;
  @Autowired private ProductJpaAdapter adapter;
  @Autowired private ProductJpaMapper mapper;

  private static ProductJpaEntity entity(String id, String sellerId, String name) {
    ProductJpaEntity entity = new ProductJpaEntity();
    entity.setId(id);
    entity.setSellerId(sellerId);
    entity.setName(name);
    entity.setDescription("Description");
    entity.setType("PHYSICAL");
    entity.setPrice(new BigDecimal("10.00"));
    entity.setStatus("DRAFT");
    entity.setVariantsJson("[]");
    entity.setSku("SKU-1");
    return entity;
  }

  private static Product product(String sellerId, String name, Product.ProductType type) {
    return Product.create(sellerId, name, "Description", type, new BigDecimal("19.99"), "SKU-1");
  }

  @Test
  @DisplayName("save and findById round-trip every mapped field")
  void saveAndFindByIdRoundTripsAllFields() {
    Product product = product("seller-1", "Laptop", Product.ProductType.PHYSICAL);
    product.addVariant("16GB");

    Product saved = adapter.save(product);
    Product found = adapter.findById(saved.getId()).orElseThrow();

    assertThat(found.getSellerId()).isEqualTo("seller-1");
    assertThat(found.getName()).isEqualTo("Laptop");
    assertThat(found.getDescription()).isEqualTo("Description");
    assertThat(found.getType()).isEqualTo(Product.ProductType.PHYSICAL);
    assertThat(found.getPrice()).isEqualByComparingTo("19.99");
    assertThat(found.getStatus()).isEqualTo(Product.ProductStatus.DRAFT);
    assertThat(found.getVariants()).containsExactly("16GB");
    assertThat(found.getSku()).isEqualTo("SKU-1");
  }

  @Test
  @DisplayName("findBySellerId returns only that seller's products")
  void findBySellerIdReturnsOwnedProducts() {
    adapter.save(product("seller-1", "Laptop", Product.ProductType.PHYSICAL));
    adapter.save(product("seller-2", "Mouse", Product.ProductType.PHYSICAL));

    assertThat(adapter.findBySellerId("seller-1"))
        .extracting(Product::getName)
        .containsExactly("Laptop");
  }

  @Test
  @DisplayName("findPublished returns only published products")
  void findPublishedReturnsOnlyPublishedProducts() {
    Product draft = adapter.save(product("seller-1", "Draft", Product.ProductType.PHYSICAL));
    Product published = product("seller-1", "Published", Product.ProductType.DIGITAL);
    published.publish();
    published = adapter.save(published);

    assertThat(adapter.findPublished())
        .extracting(Product::getId)
        .containsExactly(published.getId())
        .doesNotContain(draft.getId());
  }

  @Test
  @DisplayName("publishing a product is persisted")
  void publishingProductIsPersisted() {
    Product product = adapter.save(product("seller-1", "Laptop", Product.ProductType.PHYSICAL));
    product.publish();

    adapter.save(product);

    assertThat(adapter.findById(product.getId()).orElseThrow().getStatus())
        .isEqualTo(Product.ProductStatus.PUBLISHED);
  }

  @Test
  @DisplayName("findAll returns every persisted product")
  void findAllReturnsEveryProduct() {
    adapter.save(product("seller-1", "Laptop", Product.ProductType.PHYSICAL));
    adapter.save(product("seller-2", "eBook", Product.ProductType.DIGITAL));

    assertThat(adapter.findAll()).hasSize(2);
  }

  @Test
  @DisplayName("malformed variants JSON degrades to an empty list")
  void malformedVariantsJsonDegradesToEmptyList() {
    ProductJpaEntity entity = entity("product-1", "seller-1", "Laptop");
    entity.setVariantsJson("{not-json");
    repository.saveAndFlush(entity);

    assertThat(adapter.findById("product-1").orElseThrow().getVariants()).isEmpty();
  }

  @Test
  @DisplayName("the not-null constraint on sellerId is enforced by the database")
  void nullSellerIdIsRejected() {
    ProductJpaEntity entity = entity("product-1", null, "Laptop");

    assertThatThrownBy(() -> repository.saveAndFlush(entity))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("the not-null constraint on price is enforced by the database")
  void nullPriceIsRejected() {
    ProductJpaEntity entity = entity("product-1", "seller-1", "Laptop");
    entity.setPrice(null);

    assertThatThrownBy(() -> repository.saveAndFlush(entity))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("the mapper handles null entities defensively")
  void mapperHandlesNullEntities() {
    assertThat(mapper.toDomain(null)).isNull();
    assertThat(mapper.toEntity(null)).isNull();
  }
}
