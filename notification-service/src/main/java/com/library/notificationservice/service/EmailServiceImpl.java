package com.library.notificationservice.service;

import com.library.notificationservice.dto.EmailRequest;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${resend.api-key:#{null}}")
    private String resendApiKey;

    @Value("${resend.sender-email}")
    private String defaultSenderEmail;

    @Async("threadPoolTaskExecutor")
    @Override
    public void sendEmail(String to, String subject, String htmlContent) {
        EmailRequest request = EmailRequest.builder()
                .from(defaultSenderEmail)
                .to(new String[]{to})
                .subject(subject)
                .html(htmlContent)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(resendApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EmailRequest> entity = new HttpEntity<>(request, headers);

        try {
            logger.info("Attempting to send email to: {} with subject: {}", to, subject);
            restTemplate.postForEntity("https://api.resend.com/emails", entity, String.class);
            logger.info("Email sent successfully to: {}", to);
        } catch (HttpClientErrorException e) {
            logger.error("Client error (HTTP {}) during email sending to {}: {}", e.getStatusCode(), to, e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("General error during email sending to {}: {}", to, e.getMessage(), e);
        }
    }
}