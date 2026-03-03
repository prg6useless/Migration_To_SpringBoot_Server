package moviemate.server.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import moviemate.server.model.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByBuyerId(Integer buyerId);
}
