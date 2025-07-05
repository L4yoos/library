package com.library.notificationservice.service;

public interface EmailService {
    void sendEmail(String to, String subject, String htmlContent);
}