package moviemate.server.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moviemate.server.dto.request.CreateOrderRequest;
import moviemate.server.dto.request.UpdateOrderRequest;
import moviemate.server.dto.response.OrderResponse;
import moviemate.server.exception.UserNotFoundException;
import moviemate.server.model.Movie;
import moviemate.server.model.Order;
import moviemate.server.model.OrderProduct;
import moviemate.server.model.User;
import moviemate.server.repository.MovieRepository;
import moviemate.server.repository.OrderRepository;
import moviemate.server.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    private OrderResponse toOrderResponse(Order order) {
        List<OrderResponse.OrderProductResponse> productResponses = order.getProducts().stream()
                .map(p -> OrderResponse.OrderProductResponse.builder()
                        .movieId(p.getMovie().getId())
                        .movieTitle(p.getMovie().getTitle())
                        .quantity(p.getQuantity())
                        .price(p.getPrice())
                        .amount(p.getAmount())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyer().getId())
                .buyerName(order.getBuyer().getName())
                .name(order.getName())
                .email(order.getEmail())
                .total(order.getTotal())
                .type(order.getType())
                .status(order.getStatus())
                .products(productResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        User buyer = userRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getBuyerId()));

        List<OrderProduct> orderProducts = request.getProducts().stream()
                .map(item -> {
                    Movie movie = movieRepository.findById(item.getMovieId())
                            .orElseThrow(() -> new RuntimeException("Movie not found with id: " + item.getMovieId()));

                    BigDecimal price = movie.getPrice();
                    BigDecimal amount = price.multiply(BigDecimal.valueOf(item.getQuantity()));

                    OrderProduct op = new OrderProduct();
                    op.setMovie(movie);
                    op.setQuantity(item.getQuantity());
                    op.setPrice(price);
                    op.setAmount(amount);
                    return op;
                })
                .collect(Collectors.toList());

        BigDecimal total = orderProducts.stream()
                .map(OrderProduct::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setBuyer(buyer);
        order.setName(request.getName());
        order.setEmail(request.getEmail());
        order.setType(request.getType() != null ? request.getType() : Order.OrderType.ONLINE);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotal(total);

        orderProducts.forEach(p -> p.setOrder(order)); // link back for @OneToMany
        order.setProducts(orderProducts);

        Order saved = orderRepository.save(order);
        log.info("Order {} created for buyer {}", saved.getId(), buyer.getId());
        return toOrderResponse(saved);
    }

    // GET BY ID
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return toOrderResponse(order);
    }

    // GET MY ORDERS
    public List<OrderResponse> getMyOrders(Integer buyerId) {
        return orderRepository.findByBuyerId(buyerId).stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    // GET ALL (admin)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    // UPDATE STATUS (admin)
    public OrderResponse updateOrderStatus(String id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.setStatus(status);
        log.info("Order {} status updated to {}", id, status);
        return toOrderResponse(orderRepository.save(order));
    }

    // CANCEL
    public String cancelOrder(String id, Integer requestingUserId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (!order.getBuyer().getId().equals(requestingUserId))
            throw new RuntimeException("You are not authorized to cancel this order");
        if (order.getStatus() != Order.OrderStatus.PENDING)
            throw new RuntimeException("Only pending orders can be cancelled");

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled by user {}", id, requestingUserId);
        return "Order cancelled successfully";
    }

    public OrderResponse updateOrder(String id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (request.getType() != null)
            order.setType(request.getType());

        Order updated = orderRepository.save(order);
        log.info("Order {} updated", id);
        return toOrderResponse(updated);
    }
}
