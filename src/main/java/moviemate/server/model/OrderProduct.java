package moviemate.server.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "order_products")
@Data
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal amount;
}