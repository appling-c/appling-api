package com.juno.appling.domain.member.controller;

import com.juno.appling.BaseTest;
import com.juno.appling.domain.member.dto.*;
import com.juno.appling.domain.member.entity.Member;
import com.juno.appling.domain.member.entity.Recipient;
import com.juno.appling.config.base.ResultCode;
import com.juno.appling.domain.member.enums.RecipientInfoStatus;
import com.juno.appling.domain.member.enums.Role;
import com.juno.appling.domain.member.vo.LoginVo;
import com.juno.appling.domain.member.repository.MemberRepository;
import com.juno.appling.domain.member.repository.RecipientRepository;
import com.juno.appling.domain.member.service.MemberAuthService;
import com.juno.appling.domain.member.service.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberControllerDocs extends BaseTest {
    
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

    private final static String PREFIX = "/api/member";
    private final static String EMAIL = "juno@member.com";
    private final static String PASSWORD = "password";

    @Test
    @DisplayName(PREFIX)
    void member() throws Exception {
        //given
        LoginDto loginDto = new LoginDto(EMAIL, PASSWORD);
        LoginVo loginVo = memberAuthService.login(loginDto);
        //when
        ResultActions resultActions = mock.perform(
                get(PREFIX)
                .header(AUTHORIZATION, "Bearer "+ loginVo.accessToken())
        ).andDo(print());

        //then
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
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
                        fieldWithPath("data.role").type(JsonFieldType.STRING).description(String.format("권한 (일반 유저 : %s, 판매자 : %s)", Role.MEMBER, Role.SELLER)),
                        fieldWithPath("data.created_at").type(JsonFieldType.STRING).description("생성일"),
                        fieldWithPath("data.modified_at").type(JsonFieldType.STRING).description("수정일")
                )
        ));
    }

    @Test
    @DisplayName(PREFIX+"/seller")
    void applySeller() throws Exception {
        //given
        JoinDto joinDto = new JoinDto(EMAIL, PASSWORD, "name", "nick", "19941030");
        memberAuthService.join(joinDto);
        LoginDto loginDto = new LoginDto(EMAIL, PASSWORD);
        LoginVo loginVo = memberAuthService.login(loginDto);
        //when
        ResultActions resultActions = mock.perform(
                post(PREFIX+"/seller")
                        .header(AUTHORIZATION, "Bearer "+ loginVo.accessToken())
        ).andDo(print());

        //then
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertThat(contentAsString).contains(ResultCode.SUCCESS.code);

        resultActions.andDo(docs.document(
                requestHeaders(
                        headerWithName(AUTHORIZATION).description("access token")
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.message").type(JsonFieldType.STRING).description("성공")
                )
        ));
    }

    @Test
    @DisplayName(PREFIX)
    void patchMember() throws Exception {
        //given
        String email = "patch@appling.com";
        String password = "password";
        String changePassword = "password2";
        String changeName = "수정함";
        String changeBirth = "19941030";

        JoinDto joinDto = new JoinDto(email, password, "수정자", "수정할거야", "19991010");
        joinDto.passwordEncoder(passwordEncoder);
        memberRepository.save(Member.of(joinDto));
        LoginDto loginDto = new LoginDto(email, password);
        LoginVo loginVo = memberAuthService.login(loginDto);

        PatchMemberDto patchMemberDto = new PatchMemberDto(changeBirth, changeName, changePassword, "수정되버림", null);

        //when
        ResultActions resultActions = mock.perform(
                patch(PREFIX)
                        .header(AUTHORIZATION, "Bearer "+ loginVo.accessToken())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(patchMemberDto))
        ).andDo(print());


        //then
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
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
                        fieldWithPath("status").type(JsonFieldType.STRING).description("회원 상태(아직 사용하지 않음) ex)탈퇴").optional()
                ),
                responseFields(
                        fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                        fieldWithPath("data.message").type(JsonFieldType.STRING).description("결과 메세지")
                )
        ));
    }

    @Test
    @DisplayName(PREFIX+"/recipient (POST)")
    void postRecipient() throws Exception {
        //given
        LoginDto loginDto = new LoginDto(MEMBER_EMAIL, PASSWORD);
        LoginVo loginVo = memberAuthService.login(loginDto);
        PostRecipientDto postRecipientDto = new PostRecipientDto("수령인", "recipient@appling.com", "01012341234");
        //when
        ResultActions resultActions = mock.perform(
                post(PREFIX+"/recipient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRecipientDto))
                        .header(AUTHORIZATION, "Bearer "+ loginVo.accessToken())
        );

        //then
        resultActions.andExpect(status().is2xxSuccessful());

        resultActions.andDo(docs.document(
                requestHeaders(
                        headerWithName(AUTHORIZATION).description("access token")
                ),
                requestFields(
                        fieldWithPath("name").type(JsonFieldType.STRING).description("수령인 이름"),
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
    @DisplayName(PREFIX+"/recipient (GET)")
    @Transactional
    void getRecipient() throws Exception {
        //given
        LoginDto loginDto = new LoginDto(MEMBER_EMAIL, PASSWORD);
        LoginVo loginVo = memberAuthService.login(loginDto);
        Member member = memberRepository.findByEmail(MEMBER_EMAIL).get();

        Recipient recipient1 = Recipient.of(member, "수령인1", "주소", "01012341234", RecipientInfoStatus.NORMAL);
        Recipient recipient2 = Recipient.of(member, "수령인2", "주소2", "01012341234", RecipientInfoStatus.NORMAL);
        Recipient saveRecipient1 = recipientRepository.save(recipient1);
        Recipient saveRecipient2 = recipientRepository.save(recipient2);

        member.getRecipientList().add(saveRecipient1);
        member.getRecipientList().add(saveRecipient2);

        //when
        ResultActions resultActions = mock.perform(
                get(PREFIX+"/recipient")
                        .header(AUTHORIZATION, "Bearer "+ loginVo.accessToken())
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
                        fieldWithPath("data.list[].address").type(JsonFieldType.STRING).description("수령인 주소"),
                        fieldWithPath("data.list[].tel").type(JsonFieldType.STRING).description("수령인 연락처"),
                        fieldWithPath("data.list[].status").type(JsonFieldType.STRING).description("상태값"),
                        fieldWithPath("data.list[].created_at").type(JsonFieldType.STRING).description("생성일"),
                        fieldWithPath("data.list[].modified_at").type(JsonFieldType.STRING).description("수정일")
                )
        ));
    }

}