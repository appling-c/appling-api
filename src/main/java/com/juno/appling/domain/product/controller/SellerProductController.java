package com.juno.appling.domain.product.controller;

import com.juno.appling.config.base.Api;
import com.juno.appling.domain.product.dto.PutProductDto;
import com.juno.appling.domain.product.dto.ProductDto;
import com.juno.appling.config.base.ResultCode;
import com.juno.appling.domain.product.vo.ProductListVo;
import com.juno.appling.domain.product.vo.ProductVo;
import com.juno.appling.domain.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api-prefix}/seller/product")
@RequiredArgsConstructor
public class SellerProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Api<ProductListVo>> getProductList(@PageableDefault(size = 10, page = 0) Pageable pageable, @RequestParam(required = false, name = "search") String search, @RequestParam(required = false, name = "status", defaultValue = "normal") String status, @RequestParam(required = false, name = "category_id", defaultValue = "0") Long categoryId, HttpServletRequest request){
        return ResponseEntity.ok(Api.<ProductListVo>builder()
                .code(ResultCode.SUCCESS.code)
                .message(ResultCode.SUCCESS.message)
                .data(productService.getProductListBySeller(pageable, search, status, categoryId, request))
                .build());
    }

    @PostMapping
    public ResponseEntity<Api<ProductVo>> postProduct(@RequestBody @Validated ProductDto productDto, HttpServletRequest request, BindingResult bindingResult){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Api.<ProductVo>builder()
                        .code(ResultCode.POST.code)
                        .message(ResultCode.POST.message)
                        .data(productService.postProduct(productDto, request))
                        .build());
    }

    @PutMapping
    public ResponseEntity<Api<ProductVo>> putProduct(@RequestBody @Validated PutProductDto putProductDto, BindingResult bindingResult){
        return ResponseEntity.ok(Api.<ProductVo>builder()
                        .code(ResultCode.SUCCESS.code)
                        .message(ResultCode.SUCCESS.message)
                        .data(productService.putProduct(putProductDto))
                        .build());
    }
}
