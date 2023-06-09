package com.juno.appling.config.base;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ErrorApi<T> {
    private String code;
    private String message;
    private List<T> errors;
}
