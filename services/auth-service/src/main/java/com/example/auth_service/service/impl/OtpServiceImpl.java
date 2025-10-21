package com.example.auth_service.service.impl;

import com.example.auth_service.event.UserLoginEvent;
import com.example.auth_service.service.OtpService;
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
    private static final long OTP_EXPIRATION_MINUTES = 5;

    @Value("${sqs.user-login-queue-url}")
    private String userLoginQueueUrl;

    @Override
    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        redisTemplate.opsForValue().set(email, otp, Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        log.info("OTP generated for email: {}", otp);
        UserLoginEvent event = new UserLoginEvent(email, otp);
        sendLoginMessageToQueue(event);
        return otp;
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
            log.info("Sending otp code");
            String messageBody = objectMapper.writeValueAsString(event);
            sqsClient.sendMessage(builder -> builder.queueUrl(userLoginQueueUrl).messageBody(messageBody));
            log.info("Code sent to queue");
        }catch(Exception ex){
            log.error("Error sending login message to queue: {}", ex.getMessage());
        }
    }
}
