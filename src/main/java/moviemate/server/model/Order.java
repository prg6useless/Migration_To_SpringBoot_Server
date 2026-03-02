package moviemate.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    private String name;
    private String email;

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private OrderType type = OrderType.ONLINE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status = OrderStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> products;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderType {
        ONLINE, CASH_ON_DELIVERY
    }

    public enum OrderStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
}