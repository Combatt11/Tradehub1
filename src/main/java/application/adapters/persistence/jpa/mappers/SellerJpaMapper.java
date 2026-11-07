package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.SellerJpaEntity;
import application.domain.models.Seller;
import org.springframework.stereotype.Component;

@Component
public class SellerJpaMapper {

    public Seller toDomain(SellerJpaEntity e) {
        if (e == null) return null;
        return new Seller(e.getId(), e.getUserId(), e.getBusinessName(), e.getTaxId(),
                Seller.SellerStatus.valueOf(e.getStatus()), e.getContactPhone());
    }

    public SellerJpaEntity toEntity(Seller d) {
        if (d == null) return null;
        SellerJpaEntity e = new SellerJpaEntity();
        e.setId(d.getId());
        e.setUserId(d.getUserId());
        e.setBusinessName(d.getBusinessName());
        e.setTaxId(d.getTaxId());
        e.setStatus(d.getStatus().name());
        e.setContactPhone(d.getContactPhone());
        return e;
    }
}
