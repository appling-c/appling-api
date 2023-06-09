package com.juno.appling.domain.product.repository;

import com.juno.appling.config.querydsl.QuerydslConfig;
import com.juno.appling.domain.product.entity.Category;
import com.juno.appling.domain.product.entity.QProduct;
import com.juno.appling.domain.product.enums.Status;
import com.juno.appling.domain.product.vo.CategoryVo;
import com.juno.appling.domain.product.vo.ProductVo;
import com.juno.appling.domain.product.vo.SellerVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductCustomRepository {
    private final QuerydslConfig q;


    public Page<ProductVo> findAll(Pageable pageable, String search, Status status, Category category, Long memberId){
        QProduct product = QProduct.product;
        BooleanBuilder builder = new BooleanBuilder();

        search = Optional.ofNullable(search).orElse("").trim();
        memberId = Optional.ofNullable(memberId).orElse(0L);
        Optional<Category> optionalCategory = Optional.ofNullable(category);

        if(!search.equals("")){
            builder.and(product.mainTitle.contains(search));
        }
        if(memberId != 0L){
            builder.and(product.member.id.eq(memberId));
        }
        if(optionalCategory.isPresent()){
            builder.and(product.category.eq(category));
        }
        builder.and(product.status.eq(status));

        List<ProductVo> content = q.query().select(Projections.constructor(ProductVo.class,
                    product.id,
                    product.mainTitle,
                    product.mainExplanation,
                    product.productMainExplanation,
                    product.productSubExplanation,
                    product.originPrice,
                    product.price,
                    product.purchaseInquiry,
                    product.origin,
                    product.producer,
                    product.mainImage,
                    product.image1,
                    product.image2,
                    product.image3,
                    product.viewCnt,
                    product.status,
                    product.createAt,
                    product.modifiedAt,
                    Projections.constructor(SellerVo.class,
                        product.member.id,
                        product.member.email,
                        product.member.nickname,
                        product.member.name
                    ),
                    Projections.constructor(CategoryVo.class,
                            product.category.id,
                            product.category.name,
                            product.category.createdAt,
                            product.category.modifiedAt
                    )
                ))
                .from(product)
                .where(builder)
                .orderBy(product.createAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = q.query().from(product).where(builder).stream().count();
        return new PageImpl<>(content, pageable, total);
    }
}
