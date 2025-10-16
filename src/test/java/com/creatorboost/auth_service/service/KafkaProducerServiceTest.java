//package com.creatorboost.auth_service.service;
//
//import dto.EmailMessageDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.kafka.core.KafkaTemplate;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class KafkaProducerServiceTest {
//
//    @Mock
//    private KafkaTemplate<String, Object> kafkaTemplate;
//
//    @InjectMocks
//    private KafkaProducerService kafkaProducerService;
//
//    private final String topicName = "test-topic";
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        // Manually inject topic name since @Value won't work in a plain unit test
//        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
//        try {
//            java.lang.reflect.Field field = KafkaProducerService.class.getDeclaredField("topicName");
//            field.setAccessible(true);
//            field.set(kafkaProducerService, topicName);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } // helper to set private field
//    }
//
//    @Test
//    void sendEmailNotification_ShouldSendToKafkaTemplate() {
//        EmailMessageDto message = new EmailMessageDto(
//                "test@example.com",
//                "Test User",
//                null,
//                "WELCOME",
//                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
//                "auth-service"
//        );
//
//        when(kafkaTemplate.send(anyString(), any())).thenReturn(mock(java.util.concurrent.CompletableFuture.class));
//
//        kafkaProducerService.sendEmailNotification(message);
//
//        verify(kafkaTemplate, times(1)).send(eq(topicName), eq(message));
//    }
//
//    @Test
//    void sendWelcomeEmail_ShouldBuildAndSendMessage() {
//        when(kafkaTemplate.send(anyString(), any())).thenReturn(mock(java.util.concurrent.CompletableFuture.class));
//
//        kafkaProducerService.sendWelcomeEmail("welcome@example.com", "New User");
//
//        ArgumentCaptor<EmailMessageDto> captor = ArgumentCaptor.forClass(EmailMessageDto.class);
//        verify(kafkaTemplate).send(eq(topicName), captor.capture());
//
//        EmailMessageDto sent = captor.getValue();
//        assertEquals("welcome@example.com", sent.getToEmail());
//        assertEquals("New User", sent.getName());
//        assertEquals("WELCOME", sent.getEmailType());
//        assertEquals("auth-service", sent.getServiceFrom());
//    }
//
//    @Test
//    void sendPasswordResetOtp_ShouldBuildAndSendMessage() {
//        when(kafkaTemplate.send(anyString(), any())).thenReturn(mock(java.util.concurrent.CompletableFuture.class));
//
//        kafkaProducerService.sendPasswordResetOtp("reset@example.com", "123456");
//
//        ArgumentCaptor<EmailMessageDto> captor = ArgumentCaptor.forClass(EmailMessageDto.class);
//        verify(kafkaTemplate).send(eq(topicName), captor.capture());
//
//        EmailMessageDto sent = captor.getValue();
//        assertEquals("reset@example.com", sent.getToEmail());
//        assertEquals("123456", sent.getOtp());
//        assertEquals("PASSWORD_RESET_OTP", sent.getEmailType());
//    }
//
//    @Test
//    void sendVerificationOtp_ShouldBuildAndSendMessage() {
//        when(kafkaTemplate.send(anyString(), any())).thenReturn(mock(java.util.concurrent.CompletableFuture.class));
//
//        kafkaProducerService.sendVerificationOtp("verify@example.com", "654321");
//
//        ArgumentCaptor<EmailMessageDto> captor = ArgumentCaptor.forClass(EmailMessageDto.class);
//        verify(kafkaTemplate).send(eq(topicName), captor.capture());
//
//        EmailMessageDto sent = captor.getValue();
//        assertEquals("verify@example.com", sent.getToEmail());
//        assertEquals("654321", sent.getOtp());
//        assertEquals("VERIFICATION_OTP", sent.getEmailType());
//    }
//}
