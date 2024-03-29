package com.juno.appling.order.service;

import com.juno.appling.order.controller.request.*;
import com.juno.appling.order.controller.response.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    PostTempOrderResponse postTempOrder(TempOrderRequest tempOrderRequest, HttpServletRequest request);
    TempOrderResponse getOrderInfo(Long orderId, HttpServletRequest request);
    CompleteOrderResponse completeOrder(CompleteOrderRequest completeOrderRequest, HttpServletRequest request);
    OrderListResponse getOrderListBySeller(Pageable pageable, String search, String status, HttpServletRequest request);
    OrderListResponse getOrderListByMember(Pageable pageable, String search, String status, HttpServletRequest request);

    OrderResponse getOrderDetailBySeller(Long orderId, HttpServletRequest request);

    OrderResponse getOrderDetailByMember(Long orderId, HttpServletRequest request);

    void cancelOrder(CancelOrderRequest cancelOrderRequest, HttpServletRequest request);
    void cancelOrderBySeller(CancelOrderRequest cancelOrderRequest, HttpServletRequest request);

    void processingOrder(ProcessingOrderRequest processingOrderRequest, HttpServletRequest request);
    void confirmOrder(ConfirmOrderRequest confirmOrderRequest, HttpServletRequest request);
}
