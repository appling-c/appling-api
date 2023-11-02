package com.juno.appling.order.domain.entity;

import com.juno.appling.order.controller.request.CompleteOrderRequest;
import com.juno.appling.order.enums.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delivery")
public class DeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItemEntity orderItem;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private String ownerName;
    private String ownerZonecode;
    private String ownerAddress;
    private String ownerAddressDetail;
    private String ownerTel;

    private String recipientName;
    private String recipientZonecode;
    private String recipientAddress;
    private String recipientAddressDetail;
    private String recipientTel;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private DeliveryEntity(OrderEntity order, OrderItemEntity orderItem, DeliveryStatus status, String ownerName, String ownerZonecode, String ownerAddress, String ownerAddressDetail, String ownerTel, String recipientName, String recipientZonecode, String recipientAddress, String recipientAddressDetail, String recipientTel, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.order = order;
        this.orderItem = orderItem;
        this.status = status;
        this.ownerName = ownerName;
        this.ownerZonecode = ownerZonecode;
        this.ownerAddress = ownerAddress;
        this.ownerAddressDetail = ownerAddressDetail;
        this.ownerTel = ownerTel;
        this.recipientName = recipientName;
        this.recipientZonecode = recipientZonecode;
        this.recipientAddress = recipientAddress;
        this.recipientAddressDetail = recipientAddressDetail;
        this.recipientTel = recipientTel;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static DeliveryEntity of(OrderEntity orderEntity, OrderItemEntity oi, CompleteOrderRequest completeOrderRequest) {
        LocalDateTime now = LocalDateTime.now();
        return new DeliveryEntity(orderEntity, oi, DeliveryStatus.TEMP, completeOrderRequest.getOwnerName(), completeOrderRequest.getOwnerZonecode(), completeOrderRequest.getOwnerAddress(), completeOrderRequest.getOwnerAddressDetail(), completeOrderRequest.getOwnerTel(), completeOrderRequest.getRecipientName(), completeOrderRequest.getRecipientZonecode(), completeOrderRequest.getRecipientAddress(), completeOrderRequest.getRecipientAddressDetail(), completeOrderRequest.getRecipientTel(), now, now);
    }
}