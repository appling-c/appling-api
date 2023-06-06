package com.juno.appling.service.product;

import com.juno.appling.common.s3.S3Service;
import com.juno.appling.common.security.TokenProvider;
import com.juno.appling.domain.dto.product.ProductDto;
import com.juno.appling.domain.entity.member.Member;
import com.juno.appling.domain.entity.product.Product;
import com.juno.appling.domain.enums.member.Role;
import com.juno.appling.domain.vo.product.ProductVo;
import com.juno.appling.repository.member.MemberRepository;
import com.juno.appling.repository.product.ProductRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {
    @InjectMocks
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private Environment env;

    private MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    @DisplayName("상품 등록 성공")
    void postProduct() {
        //given
        request.addHeader(AUTHORIZATION, "Bearer token");
        String mainTitle = "메인 제목";

        ProductDto dto = ProductDto.builder()
                .mainTitle(mainTitle)
                .mainExplanation("메인 설명")
                .productMainExplanation("상품 메인 설명")
                .productSubExplanation("상품 서브 설명")
                .producer("공급자")
                .origin("원산지")
                .purchaseInquiry("취급 방법")
                .originPrice(1000)
                .price(100)
                .mainImage("https://main_image")
                .image1("https://image1")
                .image2("https://image2")
                .image3("https://image3")
                .build();

        given(tokenProvider.resolveToken(any())).willReturn("token");
        LocalDateTime now = LocalDateTime.now();
        Member member = new Member(1L, "email@mail.com", "password", "nickname", "name", "19941030", Role.SELLER, null, null, now, now);
        given(memberRepository.findById(any())).willReturn(Optional.of(member));
        given(productRepository.save(any())).willReturn(Product.of(member, dto));
        //when
        ProductVo productVo = productService.postProduct(dto, request);

        //then
        Assertions.assertThat(productVo.getMainTitle()).isEqualTo(mainTitle);
    }
}