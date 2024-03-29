package com.juno.appling.member.domain.model;

import com.juno.appling.member.enums.RecipientInfoStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Recipient {
    private Long id;
    private Member member;
    private String name;
    private String address;
    private String addressDetail;
    private String zonecode;
    private String tel;
    private RecipientInfoStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

}
