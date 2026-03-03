package moviemate.server.dto.request;

import moviemate.server.model.Order;
import lombok.Data;

@Data
public class UpdateOrderRequest {
    private Order.OrderType type;
}