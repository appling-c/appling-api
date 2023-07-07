package com.juno.appling.controller.member;

import com.juno.appling.domain.dto.Api;
import com.juno.appling.domain.dto.member.PatchMemberDto;
import com.juno.appling.domain.dto.member.PostBuyerDto;
import com.juno.appling.domain.dto.member.PostRecipientDto;
import com.juno.appling.domain.dto.member.PutBuyerDto;
import com.juno.appling.domain.vo.MessageVo;
import com.juno.appling.domain.vo.member.BuyerVo;
import com.juno.appling.domain.vo.member.MemberVo;
import com.juno.appling.domain.vo.member.RecipientListVo;
import com.juno.appling.service.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.juno.appling.domain.enums.ResultCode.POST;
import static com.juno.appling.domain.enums.ResultCode.SUCCESS;

@RestController
@RequestMapping("${api-prefix}/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("")
    public ResponseEntity<Api<MemberVo>> member(HttpServletRequest request){
        return ResponseEntity.ok(
                Api.<MemberVo>builder()
                        .code(SUCCESS.code)
                        .message(SUCCESS.message)
                        .data(memberService.member(request))
                        .build()
        );
    }

    @PostMapping("/seller")
    public ResponseEntity<Api<MessageVo>> applySeller(HttpServletRequest request){
        return ResponseEntity.ok(
                Api.<MessageVo>builder()
                        .code(SUCCESS.code)
                        .message(SUCCESS.message)
                        .data(memberService.applySeller(request))
                        .build()
        );
    }

    @PatchMapping
    public ResponseEntity<Api<MessageVo>> patchMember(@RequestBody @Validated PatchMemberDto patchMemberDto, HttpServletRequest request, BindingResult bindingResult){
        return ResponseEntity.ok(
                Api.<MessageVo>builder()
                        .code(SUCCESS.code)
                        .message(SUCCESS.message)
                        .data(memberService.patchMember(patchMemberDto ,request))
                        .build()
        );
    }

    @PostMapping("/buyer")
    public ResponseEntity<Api<MessageVo>> postBuyer(@RequestBody @Validated PostBuyerDto postBuyerDto, HttpServletRequest request, BindingResult bindingResult){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Api.<MessageVo>builder()
                        .code(POST.code)
                        .message(POST.message)
                        .data(memberService.postBuyer(postBuyerDto, request))
                        .build()
        );
    }

    @GetMapping("/buyer")
    public ResponseEntity<Api<BuyerVo>> getBuyer(HttpServletRequest request){
        return ResponseEntity.ok(Api.<BuyerVo>builder()
                        .code(SUCCESS.code)
                        .message(SUCCESS.message)
                        .data(memberService.getBuyer(request))
                        .build()
                );
    }

    @PutMapping("/buyer")
    public ResponseEntity<Api<MessageVo>> putBuyer(@RequestBody @Validated PutBuyerDto putBuyerDto, BindingResult bindingResult){
        return ResponseEntity.ok(Api.<MessageVo>builder()
                .code(SUCCESS.code)
                .message(SUCCESS.message)
                .data(memberService.putBuyer(putBuyerDto))
                .build()
        );
    }

    @PostMapping("/recipient")
    public ResponseEntity<Api<MessageVo>> postRecipient(@RequestBody @Validated PostRecipientDto postRecipientDtoInfo, HttpServletRequest request, BindingResult bindingResult){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Api.<MessageVo>builder()
                        .code(POST.code)
                        .message(POST.message)
                        .data(memberService.postRecipient(postRecipientDtoInfo, request))
                        .build()
                );
    }

    @GetMapping("/recipient")
    public ResponseEntity<Api<RecipientListVo>> getRecipientList(HttpServletRequest request){
        return ResponseEntity.ok(Api.<RecipientListVo>builder()
                        .code(SUCCESS.code)
                        .message(SUCCESS.message)
                        .data(memberService.getRecipient(request))
                        .build()
                );
    }
}
