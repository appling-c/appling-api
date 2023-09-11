package com.juno.appling.member.application;

import com.juno.appling.global.advice.exception.DuringProcessException;
import com.juno.appling.global.mail.MyMailSender;
import com.juno.appling.global.security.TokenProvider;
import com.juno.appling.member.dto.request.JoinRequest;
import com.juno.appling.member.dto.request.LoginRequest;
import com.juno.appling.member.dto.response.kakao.KakaoLoginResponse;
import com.juno.appling.member.dto.response.kakao.KakaoMemberResponse;
import com.juno.appling.member.domain.Member;
import com.juno.appling.member.enums.Role;
import com.juno.appling.member.enums.SnsJoinType;
import com.juno.appling.member.dto.response.JoinResponse;
import com.juno.appling.member.dto.response.LoginResponse;
import com.juno.appling.member.domain.MemberRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberAuthService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient kakaoClient;
    private final WebClient kakaoApiClient;
    private final Environment env;
    private final MyMailSender myMailSender;

    private static final String AUTHORITIES_KEY = "auth";
    private static final String TYPE = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30;            // 30분

    @Transactional
    public JoinResponse join(JoinRequest joinRequest) {
        joinRequest.passwordEncoder(passwordEncoder);
        Optional<Member> findMember = memberRepository.findByEmail(joinRequest.getEmail());
        if (findMember.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다.");
        }

        Member saveMember = memberRepository.save(Member.of(joinRequest));
        myMailSender.send("애플링 가족이 되신걸 환영해요!", "<html><h1>애플링 회원 가입에 감사드립니다.</h1></html>",
            saveMember.getEmail());
        return new JoinResponse(saveMember.getName(), saveMember.getNickname(), saveMember.getEmail());
    }

    public LoginResponse login(LoginRequest loginRequest) {
        // id, pw 기반으로 UsernamePasswordAuthenticationToken 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = loginRequest.toAuthentication();

        // security에 구현한 AuthService가 실행됨
        Authentication authenticate = authenticationManagerBuilder.getObject()
            .authenticate(authenticationToken);

        LoginResponse loginResponse = tokenProvider.generateTokenDto(authenticate);

        // token redis 저장
        saveRedisToken(loginResponse);

        return loginResponse;
    }

    public LoginResponse refresh(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String originAccessToken = Optional.ofNullable(ops.get(refreshToken)).orElse("").toString();
        Claims claims = tokenProvider.parseClaims(originAccessToken);

        String sub = claims.get("sub").toString();
        long now = (new Date()).getTime();
        Date accessTokenExpired = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        Member member = memberRepository.findById(Long.parseLong(sub)).orElseThrow(() ->
            new IllegalArgumentException("유효하지 않은 회원입니다.")
        );

        Role role = Role.valueOf(member.getRole().roleName);
        String[] roleSplitList = role.roleList.split(",");
        List<String> trimRoleList = Arrays.stream(roleSplitList)
            .map(r -> String.format("ROLE_%s", r.trim())).toList();
        String roleList = trimRoleList.toString().replace("[", "").replace("]", "")
            .replace(" ", "");

        String accessToken = tokenProvider.createAccessToken(String.valueOf(member.getId()),
            roleList, accessTokenExpired);

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new LoginResponse(TYPE, accessToken, refreshToken, accessTokenExpired.getTime(), null);
    }

    public LoginResponse authKakao(String code) {
        log.info("code = {}", code);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", env.getProperty("kakao.client-id"));
        map.add("redirect_url", env.getProperty("kakao.redirect-url"));
        map.add("code", code);

        KakaoLoginResponse kakaoToken = Optional.ofNullable(
            kakaoClient.post().uri(("/oauth/token"))
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .body(
                    BodyInserters.fromFormData(map)
                )
                .retrieve()
                .onStatus(http -> http.isError(), response ->
                    response.bodyToMono(String.class)
                        .handle((error, sink) ->
                            sink.error(new DuringProcessException(error))
                        )
                )
                .bodyToMono(KakaoLoginResponse.class)
                .block()
        ).orElse(new KakaoLoginResponse("", "", 0L, 0L));

        return new LoginResponse(TYPE, kakaoToken.access_token, kakaoToken.refresh_token,
            kakaoToken.expires_in, kakaoToken.refresh_token_expires_in);
    }

    @Transactional
    public LoginResponse loginKakao(String accessToken) {
        KakaoMemberResponse info = kakaoApiClient.post().uri(("/v2/user/me"))
            .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
            .header(AUTHORIZATION, TYPE + accessToken)
            .retrieve()
            .bodyToMono(KakaoMemberResponse.class)
            .blockOptional()
            .orElseThrow(() -> {
                throw new IllegalStateException("카카오에서 반환 받은 값이 존재하지 않습니다.");
            });

        boolean hasEmail = info.kakao_account.has_email;
        if (!hasEmail) {
            throw new IllegalArgumentException("이메일이 존재하지 않는 회원입니다. SNS 인증 먼저 진행해주세요.");
        }

        String email = info.kakao_account.email;
        String snsId = String.valueOf(info.id);
        Optional<Member> findMember = memberRepository.findByEmail(email);
        Member member;
        if (findMember.isEmpty()) {
            // 회원 가입 진행
            JoinRequest joinRequest = new JoinRequest(email, snsId, info.kakao_account.profile.nickname,
                info.kakao_account.profile.nickname, null);
            joinRequest.passwordEncoder(passwordEncoder);
            member = memberRepository.save(Member.of(joinRequest, snsId, SnsJoinType.KAKAO));
            myMailSender.send("애플링 가족이 되신걸 환영해요!", "<html><h1>애플링 회원 가입에 감사드립니다.</h1></html>",
                member.getEmail());
        } else {
            member = findMember.get();
        }

        Role role = Role.valueOf(member.getRole().roleName);
        String[] roleSplitList = role.roleList.split(",");
        List<SimpleGrantedAuthority> grantedList = new LinkedList<>();
        for (String r : roleSplitList) {
            grantedList.add(new SimpleGrantedAuthority(r));
        }

        // 토큰 인증 저장
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            email, snsId, grantedList);
        Authentication authenticate = authenticationManagerBuilder.getObject()
            .authenticate(authenticationToken);

        LoginResponse loginResponse = tokenProvider.generateTokenDto(authenticate);

        saveRedisToken(loginResponse);

        return loginResponse;
    }

    private void saveRedisToken(LoginResponse loginResponse) {
        String accessToken = loginResponse.getAccessToken();
        String refreshToken = loginResponse.getRefreshToken();
        Claims claims = tokenProvider.parseClaims(refreshToken);
        long refreshTokenExpired = Long.parseLong(claims.get("exp").toString());

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(refreshToken, accessToken);
        redisTemplate.expireAt(refreshToken, new Date(refreshTokenExpired * 1000L));
    }
}