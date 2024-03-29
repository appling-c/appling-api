package com.juno.appling.global.mail;

import com.juno.appling.global.advice.exception.DuringProcessException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MyMailSender {

    private final JavaMailSender javaMailSender;

    public void send(String subject, String text, String toEmail) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,
                StandardCharsets.UTF_8.name());
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setTo(toEmail);
            mimeMessageHelper.setFrom("appling@gmail.com", "애플링");
            mimeMessageHelper.setText(text, true);
//            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("email = {} 전송 실패", toEmail);
            log.error(e.getMessage());
            throw new DuringProcessException("email 전송 실패", e);
        }
        log.info("[{}] send complete!", toEmail);
    }
}
