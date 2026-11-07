package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.BuyerJpaEntity;
import application.domain.models.Buyer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BuyerJpaMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Buyer toDomain(BuyerJpaEntity e) {
        if (e == null) return null;
        List<String> addresses = new ArrayList<>();
        try {
            if (e.getAdditionalAddressesJson() != null) {
                addresses = objectMapper.readValue(e.getAdditionalAddressesJson(), new TypeReference<>() {});
            }
        } catch (Exception ignored) {}
        return new Buyer(e.getId(), e.getUserId(), e.getPrimaryAddress(), addresses,
                Buyer.CommercialStatus.valueOf(e.getCommercialStatus()));
    }

    public BuyerJpaEntity toEntity(Buyer d) {
        if (d == null) return null;
        BuyerJpaEntity e = new BuyerJpaEntity();
        e.setId(d.getId());
        e.setUserId(d.getUserId());
        e.setPrimaryAddress(d.getPrimaryAddress());
        e.setCommercialStatus(d.getCommercialStatus().name());
        try {
            e.setAdditionalAddressesJson(objectMapper.writeValueAsString(d.getAdditionalAddresses()));
        } catch (Exception ignored) {
            e.setAdditionalAddressesJson("[]");
        }
        return e;
    }
}
