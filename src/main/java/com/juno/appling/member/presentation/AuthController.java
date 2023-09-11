package com.juno.appling.member.presentation;

import com.juno.appling.global.base.Api;
import com.juno.appling.member.dto.request.JoinRequest;
import com.juno.appling.member.dto.request.LoginRequest;
import com.juno.appling.member.dto.response.JoinResponse;
import com.juno.appling.member.dto.response.LoginResponse;
import com.juno.appling.member.application.MemberAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.juno.appling.global.base.ResultCode.POST;
import static com.juno.appling.global.base.ResultCode.SUCCESS;

@RestController
@RequestMapping("${api-prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberAuthService memberAuthService;

    @PostMapping("/join")
    public ResponseEntity<Api<JoinResponse>> join(@RequestBody @Validated JoinRequest joinRequest,
                                                  BindingResult bindingResult) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Api.<JoinResponse>builder()
            .code(POST.code)
            .message(POST.message)
            .data(memberAuthService.join(joinRequest))
            .build());
    }

    @PostMapping("/login")
    public ResponseEntity<Api<LoginResponse>> login(@RequestBody @Validated LoginRequest loginRequest,
                                                    BindingResult bindingResult) {
        return ResponseEntity.ok(
            Api.<LoginResponse>builder()
                .code(SUCCESS.code)
                .message(SUCCESS.message)
                .data(memberAuthService.login(loginRequest))
                .build()
        );
    }

    @GetMapping("/refresh/{refresh_token}")
    public ResponseEntity<Api<LoginResponse>> refresh(
        @PathVariable(value = "refresh_token") String refreshToken) {
        return ResponseEntity.ok(
            Api.<LoginResponse>builder()
                .code(SUCCESS.code)
                .message(SUCCESS.message)
                .data(memberAuthService.refresh(refreshToken))
                .build()
        );
    }
}