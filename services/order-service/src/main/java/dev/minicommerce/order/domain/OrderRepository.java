package dev.minicommerce.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {

    List<CustomerOrder> findTop20ByOrderByCreatedAtDesc();
}
