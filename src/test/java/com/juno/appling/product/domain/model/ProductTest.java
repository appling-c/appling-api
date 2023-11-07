package com.juno.appling.product.domain.model;

import com.juno.appling.order.controller.request.TempOrderDto;
import com.juno.appling.product.enums.ProductType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    @DisplayName("재고가 부족하면 실패")
    void checkInStockFail1(){
        int ea = 10;
        Product product = Product.builder()
                .id(1L)
                .type(ProductType.NORMAL)
                .ea(ea)
                .build();

        TempOrderDto tempOrderDto = TempOrderDto.builder()
                .productId(1L)
                .ea(ea + 1)
                .build();

        assertThatThrownBy(() -> product.checkInStock(tempOrderDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재고가 부족")
                .hasMessageContaining(String.valueOf(ea));
    }
    @Test
    @DisplayName("정의되지 않은 상품 type 실패")
    void checkInStockFail2(){
        int ea = 100;
        Product product = Product.builder()
                .id(1L)
                .type(ProductType.OPTION)
                .optionList(new ArrayList<>())
                .ea(ea)
                .build();

        TempOrderDto tempOrderDto = TempOrderDto.builder()
                .productId(1L)
                .optionId(0L)
                .ea(ea + 1)
                .build();

        assertThatThrownBy(() -> product.checkInStock(tempOrderDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("option id = 0");
    }
}