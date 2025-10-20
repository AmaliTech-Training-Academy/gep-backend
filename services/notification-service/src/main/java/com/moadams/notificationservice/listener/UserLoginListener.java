package com.moadams.notificationservice.listener;


import com.moadams.notificationservice.event.UserLoginEvent;
import com.moadams.notificationservice.service.impl.EmailService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserLoginListener {

    private final EmailService emailService;

    @SqsListener("user-login")
    public void listenUserLogin(UserLoginEvent event){
        emailService.sendOtpEmail(event.email(), event.otp());
    }
}
