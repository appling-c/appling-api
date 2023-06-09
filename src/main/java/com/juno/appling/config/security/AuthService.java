package com.juno.appling.config.security;

import com.juno.appling.domain.member.entity.Member;
import com.juno.appling.domain.member.enums.Role;
import com.juno.appling.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("회원 인증 처리");
        Member member = memberRepository.findByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException("유효하지 않은 회원입니다.")
        );

        Role role = member.getRole();
        Set<String> roleSet = new HashSet<>();
        String roleListToString = Role.valueOf(role.roleName).roleList;
        String[] roleList = roleListToString.split(",");

        for(String r : roleList){
            roleSet.add(r.trim());
        }

        String[] roles = Arrays.copyOf(roleSet.toArray(), roleSet.size(), String[].class);

        return User.builder()
                .username(String.valueOf(member.getId()))
                .password(member.getPassword())
                .roles(roles)
                .build();
    }
}
