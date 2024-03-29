package com.juno.appling.order.controller.response;

import com.juno.appling.order.domain.model.Order;
import com.juno.appling.order.domain.model.OrderItem;
import com.juno.appling.order.domain.model.OrderProduct;
import com.juno.appling.product.domain.model.Category;
import com.juno.appling.product.domain.model.Seller;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class TempOrderResponseTest {

    @Test
    @DisplayName("OrderInfoResponse create에 성공")
    void create() {
        //given
        Order order = Order.builder()
            .id(1L)
            .orderItemList(List.of(
                OrderItem.builder()
                    .id(1L)
                    .orderProduct(OrderProduct.builder()
                        .id(2L)
                        .productId(2L)
                        .seller(Seller.builder().build())
                        .category(Category.builder().build())
                        .mainTitle("상품2")
                        .build())
                    .build(),
                OrderItem.builder()
                    .id(2L)
                    .orderProduct(OrderProduct.builder()
                        .id(1L)
                        .productId(1L)
                        .seller(Seller.builder().build())
                        .category(Category.builder().build())
                        .mainTitle("상품1")
                        .build())
                    .build()
            ))
            .build();
        //when
        TempOrderResponse tempOrderResponse = TempOrderResponse.create(order);
        //then
        Assertions.assertThat(tempOrderResponse.getOrderItemList().get(0).getProductId()).isEqualTo(2L);
    }
}