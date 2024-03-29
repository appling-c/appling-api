package com.juno.appling.order.domain.entity;

import com.juno.appling.order.domain.model.OrderProduct;
import com.juno.appling.product.domain.entity.CategoryEntity;
import com.juno.appling.product.domain.entity.SellerEntity;
import com.juno.appling.product.enums.ProductStatus;
import com.juno.appling.product.enums.ProductType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;

@Audited
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_product")
public class OrderProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;

    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    @NotAudited
    private SellerEntity seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @NotAudited
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_option_id")
    @NotAudited
    private OrderOptionEntity orderOption;

    private String mainTitle;
    private String mainExplanation;
    private String productMainExplanation;
    private String productSubExplanation;
    private int originPrice;
    private int price;
    private String purchaseInquiry;
    private String origin;
    private String producer;
    private String mainImage;
    private String image1;
    private String image2;
    private String image3;
    private Long viewCnt;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    public static OrderProductEntity from(OrderProduct orderProduct) {
        OrderProductEntity orderProductEntity = new OrderProductEntity();
        orderProductEntity.id = orderProduct.getId();
        orderProductEntity.productId = orderProduct.getProductId();
        orderProductEntity.seller = SellerEntity.from(orderProduct.getSeller());
        orderProductEntity.category = CategoryEntity.from(orderProduct.getCategory());
        orderProductEntity.orderOption = OrderOptionEntity.from(orderProduct.getOrderOption());
        orderProductEntity.mainTitle = orderProduct.getMainTitle();
        orderProductEntity.mainExplanation = orderProduct.getMainExplanation();
        orderProductEntity.productMainExplanation = orderProduct.getProductMainExplanation();
        orderProductEntity.productSubExplanation = orderProduct.getProductSubExplanation();
        orderProductEntity.originPrice = orderProduct.getOriginPrice();
        orderProductEntity.price = orderProduct.getPrice();
        orderProductEntity.purchaseInquiry = orderProduct.getPurchaseInquiry();
        orderProductEntity.origin = orderProduct.getOrigin();
        orderProductEntity.producer = orderProduct.getProducer();
        orderProductEntity.mainImage = orderProduct.getMainImage();
        orderProductEntity.image1 = orderProduct.getImage1();
        orderProductEntity.image2 = orderProduct.getImage2();
        orderProductEntity.image3 = orderProduct.getImage3();
        orderProductEntity.viewCnt = orderProduct.getViewCnt();
        orderProductEntity.status = orderProduct.getStatus();
        orderProductEntity.type = orderProduct.getType();
        orderProductEntity.createdAt = orderProduct.getCreatedAt();
        orderProductEntity.modifiedAt = orderProduct.getModifiedAt();
        return orderProductEntity;
    }

    public OrderProduct toModel() {
        return OrderProduct.builder()
            .id(id)
            .productId(productId)
            .seller(seller.toModel())
            .category(category.toModel())
            .orderOption(orderOption == null ? null : orderOption.toModel())
            .mainTitle(mainTitle)
            .mainExplanation(mainExplanation)
            .productMainExplanation(productMainExplanation)
            .productSubExplanation(productSubExplanation)
            .originPrice(originPrice)
            .price(price)
            .purchaseInquiry(purchaseInquiry)
            .origin(origin)
            .producer(producer)
            .mainImage(mainImage)
            .image1(image1)
            .image2(image2)
            .image3(image3)
            .viewCnt(viewCnt)
            .status(status)
            .createdAt(createdAt)
            .modifiedAt(modifiedAt)
            .type(type)
            .build();
    }

}
