package com.juno.appling.common.presentation;

import com.juno.appling.ControllerBaseTest;
import com.juno.appling.global.s3.S3Service;
import com.juno.appling.member.dto.request.LoginRequest;
import com.juno.appling.member.domain.Introduce;
import com.juno.appling.member.domain.Member;
import com.juno.appling.member.domain.Seller;
import com.juno.appling.member.enums.IntroduceStatus;
import com.juno.appling.member.dto.response.LoginResponse;
import com.juno.appling.member.domain.IntroduceRepository;
import com.juno.appling.member.domain.MemberRepository;
import com.juno.appling.member.domain.SellerRepository;
import com.juno.appling.member.application.MemberAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

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
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
class CommonControllerDocs extends ControllerBaseTest {

    @Autowired
    private MemberAuthService memberAuthService;

    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private IntroduceRepository introduceRepository;

    @MockBean
    private S3Service s3Service;

    private final String PREFIX = "/api/common";

    @Test
    @DisplayName(PREFIX + "/image")
    void uploadImage() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest(SELLER_EMAIL, PASSWORD);
        LoginResponse login = memberAuthService.login(loginRequest);
        List<String> list = new LinkedList<>();
        list.add("image/1/20230606/202101.png");
        given(s3Service.putObject(anyString(), anyString(), any())).willReturn(list);

        //when
        ResultActions perform = mock.perform(
            multipart(PREFIX + "/image")
                .file(new MockMultipartFile("image", "test1.png",
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    "123".getBytes(StandardCharsets.UTF_8)))
                .header(AUTHORIZATION, "Bearer " + login.getAccessToken())
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
        LoginRequest loginRequest = new LoginRequest(SELLER_EMAIL, PASSWORD);
        LoginResponse login = memberAuthService.login(loginRequest);
        List<String> list = new LinkedList<>();
        list.add("html/1/20230606/202101.html");
        given(s3Service.putObject(anyString(), anyString(), any())).willReturn(list);

        //when
        ResultActions perform = mock.perform(
            multipart(PREFIX + "/html")
                .file(new MockMultipartFile("html", "test1.html",
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    "123".getBytes(StandardCharsets.UTF_8)))
                .header(AUTHORIZATION, "Bearer " + login.getAccessToken())
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
        Member member = memberRepository.findByEmail(SELLER_EMAIL).get();
        Seller seller = sellerRepository.findByMember(member).get();
        introduceRepository.save(Introduce.of(seller, "", "https://appling-s3-bucket.s3.ap-northeast-2.amazonaws.com/html/1/20230815/172623_0.html", IntroduceStatus.USE));

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
                get(PREFIX + "/introduce/{seller_id}", seller.getId())
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