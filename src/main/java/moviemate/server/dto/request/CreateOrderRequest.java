package moviemate.server.dto.request;

import lombok.Data;
import moviemate.server.model.Order;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class CreateOrderRequest {
    @JsonProperty("buyerId")
    private Integer buyerId;
    private String name;
    private String email;
    private Order.OrderType type; // "Online" or "Cash On Delivery"
    private List<OrderProductRequest> products;

    @Data
    public static class OrderProductRequest {
        @JsonProperty("movieId")
        private Integer movieId;
        private Integer quantity;
    }
}