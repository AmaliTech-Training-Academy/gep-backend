package com.example.auth_service.service.impl;

import com.example.auth_service.event.UserLoginEvent;
import com.example.auth_service.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, UserLoginEvent> kafkaLoginTemplate;
    private static final long OTP_EXPIRATION_MINUTES = 5;
    private static final String USER_LOGIN_TOPIC = "user-login-topic";

    @Override
    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        redisTemplate.opsForValue().set(email, otp, Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        log.info("OTP generated for email: {}", otp);
        kafkaLoginTemplate.send(USER_LOGIN_TOPIC, email, new UserLoginEvent(email, otp));
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
}
