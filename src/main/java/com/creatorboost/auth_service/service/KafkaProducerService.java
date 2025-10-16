package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.io.EmailMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);


    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void sendEmailNotification(EmailMessageDto message) {
        try {
            logger.info("🚀 Sending email notification: {}", message);
            kafkaTemplate.send(topicName, message)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            logger.info("✅ Successfully sent email: {}", message.getEmailType());
                        } else {
                            logger.error("❌ Failed to send email notification", ex);
                        }
                    });
        } catch (Exception e) {
            logger.error("💥 Error while sending email notification", e);
        }
    }

    public void sendWelcomeEmail(String toEmail, String name) {
        EmailMessageDto emailMessage = new EmailMessageDto(
                toEmail,
                name,
                null,
                "WELCOME",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "auth-service"
        );
        sendEmailNotification(emailMessage);
    }

    public void sendPasswordResetOtp(String toEmail, String otp) {
        EmailMessageDto emailMessage = new EmailMessageDto(
                toEmail,
                null,
                otp,
                "PASSWORD_RESET_OTP",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "auth-service"
        );
        sendEmailNotification(emailMessage);
    }
    public void sendVerificationOtp(String toEmail, String otp) {
        EmailMessageDto emailMessage = new EmailMessageDto(
                toEmail,
                null,
                otp,
                "VERIFICATION_OTP",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "auth-service"
        );
        sendEmailNotification(emailMessage);
    }
}