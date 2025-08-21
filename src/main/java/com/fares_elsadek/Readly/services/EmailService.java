package com.fares_elsadek.Readly.services;

import com.fares_elsadek.Readly.config.properties.AppProperties;
import com.fares_elsadek.Readly.config.properties.EmailProperties;
import com.fares_elsadek.Readly.exceptions.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final EmailProperties emailProperties;

    @Async("emailExecutor")
    public void sendVerificationEmailAsync(String to,String token){
        try {
            sendVerificationEmailWithRetry(to,token);
            log.info("Verification email sent successfully to: {}", to);
        }catch (Exception ex){
            log.error("Failed to send verification email to: {}", to, ex);
            throw new EmailSendingException("Failed to send verification email", ex);
        }
    }
    @Retryable(
            retryFor = {Exception.class},
            maxAttemptsExpression = "#{@emailProperties.maxRetryAttempts}",
            backoff = @Backoff(delay = 2000,multiplier = 2)
    )
    public void sendVerificationEmailWithRetry(String to,String token) throws MessagingException {
        sendVerificationEmail(to,token);
    }

    public void sendVerificationEmail(String to,String token) throws MessagingException {
        String verifyLink = String.format("%s/api/v1/auth/verify-email?token=%s",appProperties.baseUrl(),token);
        Context ctx = new Context();
        ctx.setVariable("link",verifyLink);
        ctx.setVariable("ttlMinutes",emailProperties.verificationTokenTtlMinutes());

        String html = templateEngine.process("verify-email",ctx);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,"UTF-8");

        helper.setFrom(emailProperties.from());
        helper.setTo(to);
        helper.setSubject("Verify Your Email Address - Readly");
        helper.setText(html, true);
        mailSender.send(message);
    }
}
