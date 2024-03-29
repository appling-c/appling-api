package com.juno.appling.product.domain.model;

import com.juno.appling.member.enums.IntroduceStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Introduce {
    private Long id;
    private Seller seller;
    private String subject;
    private String url;
    private IntroduceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
