package com.juno.appling.order.port;

import com.juno.appling.order.domain.entity.OrderOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderOptionJpaRepository extends JpaRepository<OrderOptionEntity, Long> {

}
