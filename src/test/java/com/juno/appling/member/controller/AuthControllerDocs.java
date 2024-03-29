package com.juno.appling.member.controller;

import static com.juno.appling.Base.objectMapper;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.juno.appling.RestdocsBaseTest;
import com.juno.appling.global.base.ResultCode;
import com.juno.appling.global.mail.MyMailSender;
import com.juno.appling.member.controller.request.JoinRequest;
import com.juno.appling.member.controller.request.LoginRequest;
import com.juno.appling.member.controller.response.LoginResponse;
import com.juno.appling.member.service.MemberAuthService;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.Assertions;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class AuthControllerDocs extends RestdocsBaseTest {

    @Autowired
    private MemberAuthService memberAuthService;

    @MockBean
    private MyMailSender myMailSender;

    private final static String PREFIX = "/api/auth";

    @Test
    @DisplayName(PREFIX + "/join")
    void join() throws Exception {
        //given
        JoinRequest joinRequest = JoinRequest.builder()
            .email("juno@auth.com")
            .password("password")
            .name("name")
            .nickname("nick")
            .birth("19941030")
            .build();
        doNothing().when(myMailSender).send(anyString(), anyString(), anyString());

        //when
        ResultActions resultActions = mock.perform(
            post(PREFIX + "/join").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinRequest))
        ).andDo(print());

        //then
        String contentAsString = resultActions.andReturn().getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertThat(contentAsString).contains(ResultCode.POST.code);
        verify(myMailSender, times(1)).send(anyString(), anyString(), anyString());

        resultActions.andDo(docs.document(
            requestFields(
                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                fieldWithPath("birth").type(JsonFieldType.STRING).description("생년월일 ex) 19941030")
                    .optional()
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.name").type(JsonFieldType.STRING).description("이름"),
                fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("닉네임"),
                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/login")
    void login() throws Exception {
        //given
        String email = "juno2@auth.com";
        String password = "password";

        JoinRequest joinRequest = JoinRequest.builder()
            .email(email)
            .password(password)
            .name("name")
            .nickname("nick")
            .birth("19941030")
            .build();
        memberAuthService.join(joinRequest);
        LoginRequest loginRequest = LoginRequest.builder().email(email).password(password).build();

        //when
        ResultActions resultActions = mock.perform(
            post(PREFIX + "/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        ).andDo(print());

        //then
        String contentAsString = resultActions.andReturn().getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertThat(contentAsString).contains(ResultCode.SUCCESS.code);

        resultActions.andDo(docs.document(
            requestFields(
                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.type").type(JsonFieldType.STRING).description("token type"),
                fieldWithPath("data.access_token").type(JsonFieldType.STRING)
                    .description("access token"),
                fieldWithPath("data.refresh_token").type(JsonFieldType.STRING)
                    .description("refresh token (기한 : 발급일로 부터 7일)"),
                fieldWithPath("data.access_token_expired").type(JsonFieldType.NUMBER)
                    .description("access token expired")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/refresh/{refresh_token}")
    void refresh() throws Exception {
        //given
        String email = "juno3@auth.com";
        String password = "password";
        JoinRequest joinRequest = JoinRequest.builder()
            .email(email)
            .password(password)
            .name("name")
            .nickname("nick")
            .birth("19941030")
            .build();
        memberAuthService.join(joinRequest);
        LoginRequest loginRequest = LoginRequest.builder().email(email).password(password).build();
        LoginResponse loginResponse = memberAuthService.login(loginRequest);

        //when
        ResultActions resultActions = mock.perform(
            RestDocumentationRequestBuilders.get(PREFIX + "/refresh/{refresh_token}",
                loginResponse.getRefreshToken())
        ).andDo(print());

        //then
        String contentAsString = resultActions.andReturn().getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertThat(contentAsString).contains(ResultCode.SUCCESS.code);

        resultActions.andDo(docs.document(
            pathParameters(
                parameterWithName("refresh_token").description("refresh token")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.type").type(JsonFieldType.STRING).description("token type"),
                fieldWithPath("data.access_token").type(JsonFieldType.STRING)
                    .description("access token"),
                fieldWithPath("data.refresh_token").type(JsonFieldType.STRING)
                    .description("refresh token (기한 : 발급일로 부터 7일)"),
                fieldWithPath("data.access_token_expired").type(JsonFieldType.NUMBER)
                    .description("access token expired")
            )
        ));
    }
}