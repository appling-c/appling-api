package com.juno.appling.member.presentation;

import com.juno.appling.ControllerBaseTest;
import com.juno.appling.global.base.ResultCode;
import com.juno.appling.global.s3.S3Service;
import com.juno.appling.member.domain.Member;
import com.juno.appling.member.domain.Recipient;
import com.juno.appling.member.enums.RecipientInfoStatus;
import com.juno.appling.member.enums.Role;
import com.juno.appling.member.dto.response.LoginResponse;
import com.juno.appling.member.dto.request.*;
import com.juno.appling.member.domain.IntroduceRepository;
import com.juno.appling.member.domain.MemberRepository;
import com.juno.appling.member.domain.RecipientRepository;
import com.juno.appling.member.domain.SellerRepository;
import com.juno.appling.member.application.MemberAuthService;
import com.juno.appling.member.application.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberControllerDocs extends ControllerBaseTest {

    @Autowired
    private MemberAuthService memberAuthService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private IntroduceRepository introduceRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @MockBean
    private S3Service s3Service;

    private final static String PREFIX = "/api/member";
    private final static String EMAIL = "juno@member.com";
    private final static String PASSWORD = "password";

    @Test
    @DisplayName(PREFIX + " (GET)")
    void member() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest(MEMBER_EMAIL, PASSWORD);
        LoginResponse loginResponse = memberAuthService.login(loginRequest);
        //when
        ResultActions resultActions = mock.perform(
            get(PREFIX)
                .header(AUTHORIZATION, "Bearer " + loginResponse.getAccessToken())
        ).andDo(print());

        //then
        String contentAsString = resultActions.andReturn().getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertThat(contentAsString).contains(ResultCode.SUCCESS.code);

        resultActions.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.member_id").type(JsonFieldType.NUMBER).description("member id"),
                fieldWithPath("data.email").type(JsonFieldType.STRING).description("email"),
                fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("nickname"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("name"),
                fieldWithPath("data.role").type(JsonFieldType.STRING).description(
                    String.format("권한 (일반 유저 : %s, 판매자 : %s)", Role.MEMBER, Role.SELLER)),
                fieldWithPath("data.status").type(JsonFieldType.STRING).description("회원 상태"),
                fieldWithPath("data.created_at").type(JsonFieldType.STRING).description("생성일"),
                fieldWithPath("data.modified_at").type(JsonFieldType.STRING).description("수정일")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/seller (POST)")
    void postSeller() throws Exception {
        //given
        JoinRequest joinRequest = new JoinRequest(EMAIL, passwordEncoder.encode(PASSWORD), "name", "nick",
            "19941030");
        memberRepository.save(Member.of(joinRequest));
        LoginRequest loginRequest = new LoginRequest(EMAIL, PASSWORD);
        LoginResponse loginResponse = memberAuthService.login(loginRequest);

        PostSellerRequest postSellerRequest = new PostSellerRequest("판매자 이름", "010-1234-4312", "1234",
            "강원도 평창군 대화면 장미산길", "mail@mail.com");
        //when
        ResultActions resultActions = mock.perform(
            post(PREFIX + "/seller")
                .header(AUTHORIZATION, "Bearer " + loginResponse.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postSellerRequest))
        ).andDo(print());

        //then
        String contentAsString = resultActions.andReturn().getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertThat(contentAsString).contains(ResultCode.POST.code);

        resultActions.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            ),
            requestFields(
                fieldWithPath("company").type(JsonFieldType.STRING).description("회사명"),
                fieldWithPath("tel").type(JsonFieldType.STRING).description("회사 연락처"),
                fieldWithPath("zonecode").type(JsonFieldType.STRING).description("우편 주소"),
                fieldWithPath("address").type(JsonFieldType.STRING).description("회사 주소"),
                fieldWithPath("email").type(JsonFieldType.STRING).description("회사 이메일").optional()
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.message").type(JsonFieldType.STRING).description("성공")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/seller (PUT)")
    void putSeller() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest(SELLER_EMAIL, PASSWORD);
        LoginResponse loginResponse = memberAuthService.login(loginRequest);

        PutSellerRequest putSellerRequest = new PutSellerRequest("변경된 판매자 이름", "010-1234-4312", "1234",
            "강원도 평창군 대화면 장미산길", "mail@mail.com");
        //when
        ResultActions resultActions = mock.perform(
            put(PREFIX + "/seller")
                .header(AUTHORIZATION, "Bearer " + loginResponse.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putSellerRequest))
        ).andDo(print());

        //then
        String contentAsString = resultActions.andReturn().getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertThat(contentAsString).contains(ResultCode.SUCCESS.code);

        resultActions.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            ),
            requestFields(
                fieldWithPath("company").type(JsonFieldType.STRING).description("회사명"),
                fieldWithPath("tel").type(JsonFieldType.STRING).description("회사 연락처"),
                fieldWithPath("zonecode").type(JsonFieldType.STRING).description("우편 주소"),
                fieldWithPath("address").type(JsonFieldType.STRING).description("회사 주소"),
                fieldWithPath("email").type(JsonFieldType.STRING).description("회사 이메일").optional()
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.message").type(JsonFieldType.STRING).description("성공")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/seller (GET)")
    void getSeller() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest(SELLER_EMAIL, PASSWORD);
        LoginResponse loginResponse = memberAuthService.login(loginRequest);

        //when
        ResultActions resultActions = mock.perform(
            get(PREFIX + "/seller")
                .header(AUTHORIZATION, "Bearer " + loginResponse.getAccessToken())
        ).andDo(print());

        //then
        String contentAsString = resultActions.andReturn().getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertThat(contentAsString).contains(ResultCode.SUCCESS.code);

        resultActions.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.seller_id").type(JsonFieldType.NUMBER).description("seller id"),
                fieldWithPath("data.email").type(JsonFieldType.STRING).description("email"),
                fieldWithPath("data.company").type(JsonFieldType.STRING).description("회사명"),
                fieldWithPath("data.zonecode").type(JsonFieldType.STRING).description("우편 주소"),
                fieldWithPath("data.address").type(JsonFieldType.STRING).description("회사 주소"),
                fieldWithPath("data.tel").type(JsonFieldType.STRING).description("회사 연락처")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + " (PATCH)")
    void patchMember() throws Exception {
        //given
        String email = "patch@appling.com";
        String password = "password";
        String changePassword = "password2";
        String changeName = "수정함";
        String changeBirth = "19941030";

        JoinRequest joinRequest = new JoinRequest(email, password, "수정자", "수정할거야", "19991010");
        joinRequest.passwordEncoder(passwordEncoder);
        memberRepository.save(Member.of(joinRequest));
        LoginRequest loginRequest = new LoginRequest(email, password);
        LoginResponse loginResponse = memberAuthService.login(loginRequest);

        PatchMemberRequest patchMemberRequest = new PatchMemberRequest(changeBirth, changeName, changePassword,
            "수정되버림", null);

        //when
        ResultActions resultActions = mock.perform(
            patch(PREFIX)
                .header(AUTHORIZATION, "Bearer " + loginResponse.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(patchMemberRequest))
        ).andDo(print());

        //then
        String contentAsString = resultActions.andReturn().getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertThat(contentAsString).contains(ResultCode.SUCCESS.code);

        resultActions.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            ),
            requestFields(
                fieldWithPath("birth").type(JsonFieldType.STRING).description("생일").optional(),
                fieldWithPath("name").type(JsonFieldType.STRING).description("이름").optional(),
                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임").optional(),
                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호").optional(),
                fieldWithPath("status").type(JsonFieldType.STRING)
                    .description("회원 상태(아직 사용하지 않음) ex)탈퇴").optional()
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.message").type(JsonFieldType.STRING).description("결과 메세지")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/recipient (POST)")
    void postRecipient() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest(MEMBER_EMAIL, PASSWORD);
        LoginResponse loginResponse = memberAuthService.login(loginRequest);
        PostRecipientRequest postRecipientRequest = new PostRecipientRequest("수령인", "1234",
            "recipient@appling.com", "01012341234");
        //when
        ResultActions resultActions = mock.perform(
            post(PREFIX + "/recipient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRecipientRequest))
                .header(AUTHORIZATION, "Bearer " + loginResponse.getAccessToken())
        );

        //then
        resultActions.andExpect(status().is2xxSuccessful());

        resultActions.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            ),
            requestFields(
                fieldWithPath("name").type(JsonFieldType.STRING).description("수령인 이름"),
                fieldWithPath("zonecode").type(JsonFieldType.STRING).description("우편 주소"),
                fieldWithPath("address").type(JsonFieldType.STRING).description("수령인 주소"),
                fieldWithPath("tel").type(JsonFieldType.STRING).description("수령인 전화번호")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.message").type(JsonFieldType.STRING).description("결과 메세지")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/recipient (GET)")
    @Transactional
    void getRecipient() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest(MEMBER_EMAIL, PASSWORD);
        LoginResponse loginResponse = memberAuthService.login(loginRequest);
        Member member = memberRepository.findByEmail(MEMBER_EMAIL).get();

        Recipient recipient1 = Recipient.of(member, "수령인1", "1234", "주소", "01012341234",
            RecipientInfoStatus.NORMAL);
        Recipient recipient2 = Recipient.of(member, "수령인2", "1234", "주소2", "01012341234",
            RecipientInfoStatus.NORMAL);
        Recipient saveRecipient1 = recipientRepository.save(recipient1);
        Recipient saveRecipient2 = recipientRepository.save(recipient2);

        member.getRecipientList().add(saveRecipient1);
        member.getRecipientList().add(saveRecipient2);

        //when
        ResultActions resultActions = mock.perform(
            get(PREFIX + "/recipient")
                .header(AUTHORIZATION, "Bearer " + loginResponse.getAccessToken())
        );

        //then
        resultActions.andExpect(status().is2xxSuccessful());

        resultActions.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.list[].id").type(JsonFieldType.NUMBER).description("id"),
                fieldWithPath("data.list[].name").type(JsonFieldType.STRING).description("수령인 이름"),
                fieldWithPath("data.list[].zonecode").type(JsonFieldType.STRING)
                    .description("우편 주소"),
                fieldWithPath("data.list[].address").type(JsonFieldType.STRING)
                    .description("수령인 주소"),
                fieldWithPath("data.list[].tel").type(JsonFieldType.STRING).description("수령인 연락처"),
                fieldWithPath("data.list[].status").type(JsonFieldType.STRING).description("상태값"),
                fieldWithPath("data.list[].created_at").type(JsonFieldType.STRING)
                    .description("생성일"),
                fieldWithPath("data.list[].modified_at").type(JsonFieldType.STRING)
                    .description("수정일")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/seller/introduce (POST)")
    void postSellerIntroduce() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest(SELLER_EMAIL, PASSWORD);
        LoginResponse loginResponse = memberAuthService.login(loginRequest);

        PostIntroduceRequest postIntroduceRequest = new PostIntroduceRequest("제목",
            "https://www.s3.com/test.html");
        //when
        ResultActions resultActions = mock.perform(
            post(PREFIX + "/seller/introduce")
                .header(AUTHORIZATION, "Bearer " + loginResponse.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postIntroduceRequest))
        );
        //then
        resultActions.andExpect(status().is2xxSuccessful());

        resultActions.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            ),
            requestFields(
                fieldWithPath("subject").type(JsonFieldType.STRING).description("제목"),
                fieldWithPath("url").type(JsonFieldType.STRING).description("url")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.message").type(JsonFieldType.STRING).description("등록 결과 메세지")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/seller/introduce (GET)")
    @Transactional
    void getSellerIntroduce() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest(SELLER2_EMAIL, PASSWORD);
        LoginResponse loginResponse = memberAuthService.login(loginRequest);
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
            get(PREFIX + "/seller/introduce")
                .header(AUTHORIZATION, "Bearer " + loginResponse.getAccessToken())
        );
        //then
        resultActions.andExpect(status().is2xxSuccessful());

        resultActions.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token")
            )
        ));
    }
}