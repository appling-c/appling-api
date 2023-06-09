package com.juno.appling.domain.product.controller;

import com.juno.appling.BaseTest;
import com.juno.appling.domain.product.dto.AddViewCntDto;
import com.juno.appling.domain.product.dto.ProductDto;
import com.juno.appling.domain.member.entity.Member;
import com.juno.appling.domain.product.entity.Category;
import com.juno.appling.domain.product.entity.Product;
import com.juno.appling.domain.member.repository.MemberRepository;
import com.juno.appling.domain.product.repository.CategoryRepository;
import com.juno.appling.domain.product.repository.ProductRepository;
import com.juno.appling.domain.member.service.MemberAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerDocs extends BaseTest {
    @Autowired
    private MemberAuthService memberAuthService;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;


    private final static String PREFIX = "/api/product";

    @Test
    @DisplayName(PREFIX)
    void getProductList() throws Exception{
        //given
        Member member = memberRepository.findByEmail(SELLER_EMAIL).get();
        Category category = categoryRepository.findById(1L).get();
        ProductDto productDto = new ProductDto(1L, "메인 제목", "메인 설명", "상품 메인 설명", "상품 서브 설명", 10000, 8000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2", "https://image3", "normal");
        ProductDto searchDto = new ProductDto(1L,"검색 제목", "메인 설명", "상품 메인 설명", "상품 서브 설명", 10000, 8000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2", "https://image3", "normal");
        productRepository.save(Product.of(member, category, searchDto));

        for(int i=0; i<25; i++){
            productRepository.save(Product.of(member, category, productDto));
        }

        for(int i=0; i<10; i++){
            productRepository.save(Product.of(member, category, searchDto));
        }
        //when
        ResultActions perform = mock.perform(
                get(PREFIX)
                        .param("search", "검색")
                        .param("page", "0")
                        .param("size", "5")
                        .param("status", "normal")
                        .param("category_id", "0")
        );
        //then
        perform.andExpect(status().is2xxSuccessful());
        perform.andDo(docs.document(
                queryParameters(
                        parameterWithName("page").description("paging 시작 페이지 번호").optional(),
                        parameterWithName("size").description("paging 시작 페이지 기준 개수 크기").optional(),
                        parameterWithName("search").description("검색어").optional(),
                        parameterWithName("status").description("상품 상태값 (일반:normal, 숨김:hidden, 삭제:delete / 대소문자 구분 없음)").optional(),
                        parameterWithName("category_id").description("카테고리 검색 id (0:전체, 1:과일, 2:야채, 3:육류)").optional()
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.total_page").type(JsonFieldType.NUMBER).description("페이지 총 개수"),
                        fieldWithPath("data.total_elements").type(JsonFieldType.NUMBER).description("총 데이터수"),
                        fieldWithPath("data.number_of_elements").type(JsonFieldType.NUMBER).description("페이지 총 데이터수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("페이지 마지막 데이터 여부"),
                        fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("페이지 데이터 존재 여부"),
                        fieldWithPath("data.list[]").type(JsonFieldType.ARRAY).description("등록 상품 list"),
                        fieldWithPath("data.list[].id").type(JsonFieldType.NUMBER).description("등록 상품 id"),
                        fieldWithPath("data.list[].main_title").type(JsonFieldType.STRING).description("메인 타이틀"),
                        fieldWithPath("data.list[].main_explanation").type(JsonFieldType.STRING).description("메인 설명"),
                        fieldWithPath("data.list[].product_main_explanation").type(JsonFieldType.STRING).description("상품 메인 설명"),
                        fieldWithPath("data.list[].product_sub_explanation").type(JsonFieldType.STRING).description("상품 서브 설명"),
                        fieldWithPath("data.list[].origin_price").type(JsonFieldType.NUMBER).description("상품 원가"),
                        fieldWithPath("data.list[].price").type(JsonFieldType.NUMBER).description("상품 실제 판매 가격"),
                        fieldWithPath("data.list[].purchase_inquiry").type(JsonFieldType.STRING).description("취급 방법"),
                        fieldWithPath("data.list[].origin").type(JsonFieldType.STRING).description("원산지"),
                        fieldWithPath("data.list[].producer").type(JsonFieldType.STRING).description("공급자"),
                        fieldWithPath("data.list[].main_image").type(JsonFieldType.STRING).description("메인 이미지 url"),
                        fieldWithPath("data.list[].image1").type(JsonFieldType.STRING).description("이미지1"),
                        fieldWithPath("data.list[].image2").type(JsonFieldType.STRING).description("이미지2"),
                        fieldWithPath("data.list[].image3").type(JsonFieldType.STRING).description("이미지3"),
                        fieldWithPath("data.list[].view_cnt").type(JsonFieldType.NUMBER).description("조회수"),
                        fieldWithPath("data.list[].status").type(JsonFieldType.STRING).description("상품 상태값 (일반:normal, 숨김:hidden, 삭제:delete / 대소문자 구분 없음)"),
                        fieldWithPath("data.list[].created_at").type(JsonFieldType.STRING).description("등록일"),
                        fieldWithPath("data.list[].modified_at").type(JsonFieldType.STRING).description("수정일"),
                        fieldWithPath("data.list[].seller.member_id").type(JsonFieldType.NUMBER).description("판매자 id"),
                        fieldWithPath("data.list[].seller.email").type(JsonFieldType.STRING).description("판매자 email"),
                        fieldWithPath("data.list[].seller.nickname").type(JsonFieldType.STRING).description("판매자 닉네임"),
                        fieldWithPath("data.list[].seller.name").type(JsonFieldType.STRING).description("판매자 이름"),
                        fieldWithPath("data.list[].category.category_id").type(JsonFieldType.NUMBER).description("카테고리 id"),
                        fieldWithPath("data.list[].category.name").type(JsonFieldType.STRING).description("카테고리 명"),
                        fieldWithPath("data.list[].category.created_at").type(JsonFieldType.STRING).description("카테고리 생성일"),
                        fieldWithPath("data.list[].category.modified_at").type(JsonFieldType.STRING).description("카테고리 수정일")
                )
        ));
    }

    @Test
    @DisplayName(PREFIX+"/{id}")
    void getProduct() throws Exception{
        //given
        Member member = memberRepository.findByEmail(SELLER_EMAIL).get();
        Category category = categoryRepository.findById(1L).get();
        ProductDto productDto = new ProductDto(1L, "메인 제목", "메인 설명", "상품 메인 설명", "상품 서브 설명", 10000, 8000, "보관 방법", "원산지", "생산자", "https://mainImage", "https://image1", "https://image2", "https://image3", "normal");
        Product product = productRepository.save(Product.of(member, category, productDto));
        //when
        ResultActions perform = mock.perform(
                RestDocumentationRequestBuilders.get(PREFIX+"/{id}", product.getId())
        );
        //then
        perform.andExpect(status().is2xxSuccessful());
        perform.andDo(docs.document(
                pathParameters(
                        parameterWithName("id").description("상품 id")
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("등록 상품 id"),
                        fieldWithPath("data.main_title").type(JsonFieldType.STRING).description("메인 타이틀"),
                        fieldWithPath("data.main_explanation").type(JsonFieldType.STRING).description("메인 설명"),
                        fieldWithPath("data.product_main_explanation").type(JsonFieldType.STRING).description("상품 메인 설명"),
                        fieldWithPath("data.product_sub_explanation").type(JsonFieldType.STRING).description("상품 서브 설명"),
                        fieldWithPath("data.origin_price").type(JsonFieldType.NUMBER).description("상품 원가"),
                        fieldWithPath("data.price").type(JsonFieldType.NUMBER).description("상품 실제 판매 가격"),
                        fieldWithPath("data.purchase_inquiry").type(JsonFieldType.STRING).description("취급 방법"),
                        fieldWithPath("data.origin").type(JsonFieldType.STRING).description("원산지"),
                        fieldWithPath("data.producer").type(JsonFieldType.STRING).description("공급자"),
                        fieldWithPath("data.main_image").type(JsonFieldType.STRING).description("메인 이미지 url"),
                        fieldWithPath("data.image1").type(JsonFieldType.STRING).description("이미지1"),
                        fieldWithPath("data.image2").type(JsonFieldType.STRING).description("이미지2"),
                        fieldWithPath("data.image3").type(JsonFieldType.STRING).description("이미지3"),
                        fieldWithPath("data.created_at").type(JsonFieldType.STRING).description("등록일"),
                        fieldWithPath("data.modified_at").type(JsonFieldType.STRING).description("수정일"),
                        fieldWithPath("data.view_cnt").type(JsonFieldType.NUMBER).description("조회수"),
                        fieldWithPath("data.status").type(JsonFieldType.STRING).description("상품 상태값 (일반:normal, 숨김:hidden, 삭제:delete / 대소문자 구분 없음)"),
                        fieldWithPath("data.seller.member_id").type(JsonFieldType.NUMBER).description("판매자 id"),
                        fieldWithPath("data.seller.email").type(JsonFieldType.STRING).description("판매자 email"),
                        fieldWithPath("data.seller.nickname").type(JsonFieldType.STRING).description("판매자 닉네임"),
                        fieldWithPath("data.seller.name").type(JsonFieldType.STRING).description("판매자 이름"),
                        fieldWithPath("data.category.category_id").type(JsonFieldType.NUMBER).description("카테고리 id"),
                        fieldWithPath("data.category.name").type(JsonFieldType.STRING).description("카테고리 명"),
                        fieldWithPath("data.category.created_at").type(JsonFieldType.STRING).description("카테고리 생성일"),
                        fieldWithPath("data.category.modified_at").type(JsonFieldType.STRING).description("카테고리 수정일")
                )
        ));
    }


    @Test
    @DisplayName(PREFIX+"/category")
    void getCategoryList() throws Exception{
        //given
        //when
        ResultActions perform = mock.perform(
                RestDocumentationRequestBuilders.get(PREFIX+"/category")
        );
        //then
        perform.andExpect(status().is2xxSuccessful());
        perform.andDo(docs.document(
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.list[].category_id").type(JsonFieldType.NUMBER).description("카테고리 id"),
                        fieldWithPath("data.list[].name").type(JsonFieldType.STRING).description("카테고리 명"),
                        fieldWithPath("data.list[].created_at").type(JsonFieldType.STRING).description("카테고리 생성일"),
                        fieldWithPath("data.list[].modified_at").type(JsonFieldType.STRING).description("카테고리 수정일")
                )
        ));
    }

    @Test
    @DisplayName(PREFIX+"/cnt")
    void addViewCnt() throws Exception{
        //given
        Member member = memberRepository.findByEmail("seller@appling.com").get();
        Category category = categoryRepository.findById(1L).get();
        ProductDto productDto = new ProductDto(1L, "메인 제목", "메인 설명", "상품 메인 설명", "상품 서브 설명", 10000, 8000, "보관 방법", "원산지", "생산자", "https://mainImage", null, null, null, "normal");

        Product product = productRepository.save(Product.of(member, category, productDto));
        //when
        ResultActions perform = mock.perform(
                patch(PREFIX + "/cnt")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new AddViewCntDto(product.getId())))
        );
        //then
        perform.andExpect(status().is2xxSuccessful());
        perform.andDo(docs.document(
                requestFields(
                        fieldWithPath("product_id").type(JsonFieldType.NUMBER).description("상품 id")
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.message").type(JsonFieldType.STRING).description("조회수 등록 여부 메세지")
                )
        ));
    }
}