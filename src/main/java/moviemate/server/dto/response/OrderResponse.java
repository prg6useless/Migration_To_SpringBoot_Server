package moviemate.server.dto.response;

import moviemate.server.model.Order;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private Integer buyerId;
    private String buyerName;
    private String name;
    private String email;
    private BigDecimal total;
    private Order.OrderType type;
    private Order.OrderStatus status;
    private List<OrderProductResponse> products;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class OrderProductResponse {
        private Integer movieId;
        private String movieTitle;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal amount;
    }
}