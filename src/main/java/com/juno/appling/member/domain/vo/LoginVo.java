package com.juno.appling.member.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record LoginVo(
    String type, String accessToken,
    String refreshToken,
    Long accessTokenExpired,
    Long refreshTokenExpired
) {

}
