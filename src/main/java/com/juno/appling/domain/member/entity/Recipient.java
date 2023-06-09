package com.juno.appling.domain.member.entity;

import com.juno.appling.domain.member.enums.RecipientInfoStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor()
public class Recipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @NotNull
    private String name;
    @NotNull
    private String address;
    @NotNull
    private String tel;

    @Enumerated(EnumType.STRING)
    private RecipientInfoStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private Recipient(Member member, @NotNull String name, @NotNull String address, @NotNull String tel, RecipientInfoStatus status, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.member = member;
        this.name = name;
        this.address = address;
        this.tel = tel;
        this.status = status;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static Recipient of(Member member, @NotNull String name, @NotNull String address, @NotNull String tel, RecipientInfoStatus status){
        tel = tel.replaceAll("-", "");
        LocalDateTime now = LocalDateTime.now();
        return new Recipient(member, name, address, tel, status, now, now);
    }
}
