package com.juno.appling.domain.product.vo;

import com.juno.appling.domain.product.entity.Product;
import com.juno.appling.config.base.BaseVo;
import com.juno.appling.domain.product.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ProductVo extends BaseVo {
    private Long id;
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
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private SellerVo seller;
    private CategoryVo category;

    public static ProductVo productReturnVo(Product product){
        return ProductVo.builder()
                .id(product.getId())
                .mainTitle(product.getMainTitle())
                .mainExplanation(product.getMainExplanation())
                .productMainExplanation(product.getProductMainExplanation())
                .productSubExplanation(product.getProductSubExplanation())
                .purchaseInquiry(product.getPurchaseInquiry())
                .producer(product.getProducer())
                .origin(product.getOrigin())
                .originPrice(product.getOriginPrice())
                .price(product.getPrice())
                .mainImage(product.getMainImage())
                .image1(product.getImage1())
                .image2(product.getImage2())
                .image3(product.getImage3())
                .viewCnt(product.getViewCnt())
                .status(product.getStatus())
                .category(CategoryVo.builder()
                        .categoryId(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .createdAt(product.getCategory().getCreatedAt())
                        .modifiedAt(product.getCategory().getModifiedAt())
                        .build())
                .createdAt(product.getCreateAt())
                .modifiedAt(product.getModifiedAt())
                .build();
    }

    public void putSeller(SellerVo seller){
        this.seller = seller;
    }

}