package com.example.auth_service.service.impl;

import com.example.auth_service.service.OtpService;
import com.example.common_libraries.dto.queue_events.ResetPasswordEvent;
import com.example.common_libraries.dto.queue_events.UserLoginEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private static final long LOGIN_OTP_EXPIRATION_MINUTES = 5;
    private static final long RESET_PASSWORD_OTP_EXPIRATION_MINUTES = 5;

    @Value("${sqs.user-login-queue-url}")
    private String userLoginQueueUrl;

    @Value("${PASSWORD_RESET_QUEUE}")
    private String passwordResetQueueUrl;

    private String generateOtp(String email, long expirationMinutes) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        redisTemplate.opsForValue().set(email, otp, Duration.ofMinutes(expirationMinutes));
        return otp;
    }

    @Override
    public void requestLoginOtp(String email){
        String otp = generateOtp(email, LOGIN_OTP_EXPIRATION_MINUTES);
        UserLoginEvent event = new UserLoginEvent(email, otp);
        sendLoginMessageToQueue(event);
    }

    @Override
    public void requestResetPasswordOtp(String email, String fullName){
        String otp = generateOtp(email, RESET_PASSWORD_OTP_EXPIRATION_MINUTES);
        ResetPasswordEvent event = new ResetPasswordEvent(email, fullName, otp);
        sendResetPasswordMessageToQueue(event);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        Object storedOtp = redisTemplate.opsForValue().get(email);
        if (storedOtp != null && storedOtp.equals(otp) ) {
            redisTemplate.delete(email);
            return true;
        }
        return false;
    }

    public void sendLoginMessageToQueue(UserLoginEvent event){
        try{
            String messageBody = objectMapper.writeValueAsString(event);
            sendMessageToQueue(messageBody, userLoginQueueUrl);
        }catch(Exception ex){
            log.error("Error sending login message to queue: {}", ex.getMessage());
        }
    }

    public void sendResetPasswordMessageToQueue(ResetPasswordEvent event){
        try{
            String messageBody = objectMapper.writeValueAsString(event);
            sendMessageToQueue(messageBody, passwordResetQueueUrl);
        }catch(Exception ex){
            log.error("Error sending reset password message to queue: {}", ex.getMessage());
        }
    }

    private void sendMessageToQueue(String messageBody, String queueUrl){
        try{
            log.info("Sending message to queue {}: {}", queueUrl, messageBody);
            sqsClient.sendMessage(builder -> builder.queueUrl(queueUrl).messageBody(messageBody));
            log.info("Message sent to SQS queue");
        }catch(Exception ex){
            log.error("Error sending message to queue: {}", ex.getMessage());
        }
    }
}
