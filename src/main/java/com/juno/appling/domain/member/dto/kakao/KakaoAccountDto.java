package com.juno.appling.domain.member.dto.kakao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoAccountDto {
    public boolean has_email;
    public String email;
    public KakaoProfile profile;
}
