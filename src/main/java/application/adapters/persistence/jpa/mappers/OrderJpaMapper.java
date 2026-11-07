package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderJpaMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Order toDomain(OrderJpaEntity e) {
        if (e == null) return null;
        List<OrderItem> items = new ArrayList<>();
        try {
            if (e.getItemsJson() != null) {
                items = objectMapper.readValue(e.getItemsJson(), new TypeReference<>() {});
            }
        } catch (Exception ignored) {}
        return new Order(e.getId(), e.getBuyerId(), Order.OrderStatus.valueOf(e.getStatus()),
                items, e.getTotalAmount(), e.getShippingAddress(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getPaymentReference());
    }

    public OrderJpaEntity toEntity(Order d) {
        if (d == null) return null;
        OrderJpaEntity e = new OrderJpaEntity();
        e.setId(d.getId());
        e.setBuyerId(d.getBuyerId());
        e.setStatus(d.getStatus().name());
        e.setTotalAmount(d.getTotalAmount());
        e.setShippingAddress(d.getShippingAddress());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        e.setPaymentReference(d.getPaymentReference());
        try {
            e.setItemsJson(objectMapper.writeValueAsString(d.getItems()));
        } catch (Exception ignored) {
            e.setItemsJson("[]");
        }
        return e;
    }
}
