package com.library.notificationservice.service;

import com.library.notificationservice.dto.EmailRequest;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final RestTemplate restTemplate;

    @Value("${resend.api-key}")
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
            restTemplate.postForEntity("https://api.resend.com/emails", entity, String.class);
            System.out.println("E-mail wysłany pomyślnie do: " + to);
            //TODO LOGGER
        } catch (HttpClientErrorException e) {
            System.err.println("Błąd klienta (HTTP " + e.getStatusCode() + ") podczas wysyłania e-maila do " + to + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Ogólny błąd podczas wysyłania e-maila do " + to + ": " + e.getMessage());
        }
    }
}