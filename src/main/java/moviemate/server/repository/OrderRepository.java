package moviemate.server.repository;

// import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.transaction.annotation.Transactional;

import moviemate.server.model.Order;

public interface OrderRepository extends JpaRepository<Order, String> {

}
