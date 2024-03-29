package com.juno.appling.common.controller;

import static com.juno.appling.Base.SELLER_EMAIL;
import static com.juno.appling.Base.SELLER_LOGIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.juno.appling.RestdocsBaseTest;
import com.juno.appling.global.s3.S3Service;
import com.juno.appling.product.domain.entity.IntroduceEntity;
import com.juno.appling.member.domain.entity.MemberEntity;
import com.juno.appling.product.domain.entity.SellerEntity;
import com.juno.appling.member.enums.IntroduceStatus;
import com.juno.appling.member.port.IntroduceJpaRepository;
import com.juno.appling.member.port.MemberJpaRepository;
import com.juno.appling.product.port.SellerJpaRepository;
import com.juno.appling.member.service.MemberAuthService;
import com.juno.appling.product.port.CategoryJpaRepository;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@SqlGroup({
    @Sql(scripts = {"/sql/init.sql", "/sql/introduce.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
})
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class CommonControllerDocs extends RestdocsBaseTest {

    @Autowired
    private MemberAuthService memberAuthService;

    @Autowired
    private SellerJpaRepository sellerJpaRepository;
    @Autowired
    private MemberJpaRepository memberJpaRepository;
    @Autowired
    private IntroduceJpaRepository introduceJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @MockBean
    private S3Service s3Service;

    private final String PREFIX = "/api/common";

    @AfterEach
    void cleanup() {
        categoryJpaRepository.deleteAll();
        introduceJpaRepository.deleteAll();
        sellerJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }
    @Test
    @DisplayName(PREFIX + "/image")
    void uploadImage() throws Exception {
        //given
        List<String> list = new LinkedList<>();
        list.add("image/1/20230606/202101.png");
        given(s3Service.putObject(anyString(), anyString(), any())).willReturn(list);

        //when
        ResultActions perform = mock.perform(
            multipart(PREFIX + "/image")
                .file(new MockMultipartFile("image", "test1.png",
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    "123".getBytes(StandardCharsets.UTF_8)))
                .header(AUTHORIZATION, "Bearer " + SELLER_LOGIN.getAccessToken())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        );

        //then
        perform.andExpect(status().is2xxSuccessful());

        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            ),
            requestParts(
                partWithName("image").description("업로드 이미지")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.url").type(JsonFieldType.STRING).description("업로드 된 이미지 url")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/html")
    void uploadHtml() throws Exception {
        //given
        List<String> list = new LinkedList<>();
        list.add("html/1/20230606/202101.html");
        given(s3Service.putObject(anyString(), anyString(), any())).willReturn(list);

        //when
        ResultActions perform = mock.perform(
            multipart(PREFIX + "/html")
                .file(new MockMultipartFile("html", "test1.html",
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    "123".getBytes(StandardCharsets.UTF_8)))
                .header(AUTHORIZATION, "Bearer " + SELLER_LOGIN.getAccessToken())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        );

        //then
        perform.andExpect(status().is2xxSuccessful());

        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            ),
            requestParts(
                partWithName("html").description("업로드 html 파일")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.url").type(JsonFieldType.STRING).description("업로드 된 html url")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/introduce (GET)")
    void getSellerIntroduce() throws Exception {
        //given
        MemberEntity memberEntity = memberJpaRepository.findByEmail(SELLER_EMAIL).get();
        SellerEntity sellerEntity = sellerJpaRepository.findByMember(memberEntity).get();
        introduceJpaRepository.save(IntroduceEntity.of(sellerEntity, "", "https://appling-s3-bucket.s3.ap-northeast-2.amazonaws.com/html/1/20230815/172623_0.html", IntroduceStatus.USE));

        given(s3Service.getObject(anyString(), anyString())).willReturn("<!doctype html>\n" +
                "<html>\n" +
                "\n" +
                "<head>\n" +
                "\t<title>appling</title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "\t<H2>example 1-2</H2>\n" +
                "\t<HR>\n" +
                "\texample 1-2\n" +
                "</body>\n" +
                "\n" +
                "</html>");
        //when
        ResultActions resultActions = mock.perform(
                get(PREFIX + "/introduce/{seller_id}", sellerEntity.getId())
        );
        //then
        resultActions.andExpect(status().is2xxSuccessful());

        resultActions.andDo(docs.document(
                pathParameters(
                        parameterWithName("seller_id").description("seller id")
                )
        ));
    }
}