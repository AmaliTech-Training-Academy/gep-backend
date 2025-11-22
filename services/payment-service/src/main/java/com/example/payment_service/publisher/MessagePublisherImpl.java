package com.example.payment_service.publisher;

import com.example.common_libraries.dto.queue_events.PaymentStatusEvent;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePublisherImpl implements MessagePublisher{
    private final ObjectMapper objectMapper;
    private final SqsClient sqsClient;

    @Value("${sqs.payment-completed-event-queue-url}")
    private String paymentCompletedEventQueueUrl;

    @Value("${sqs.payment-status-queue}")
    private String paymentStatusQueue;

    @Override
    public void publishPaymentStatusToQueue(PaymentStatusEvent statusEvent) {
        try{
            log.info("Sending payment successful email to SQS");
            String messageBody = objectMapper.writeValueAsString(statusEvent);
            sqsClient.sendMessage(builder -> builder.queueUrl(paymentStatusQueue).messageBody(messageBody));
        }catch (Exception e){
            log.error("Error sending payment successful email to SQS: {}", e.getMessage());
        }
    }

    @Override
    public void publishPaymentSuccessfulEventToQueue(ProcessPaymentEvent event) {
        try{
            log.info("Sending payment successful event to SQS");
            String messageBody = objectMapper.writeValueAsString(event);
            sqsClient.sendMessage(builder -> builder.queueUrl(paymentCompletedEventQueueUrl).messageBody(messageBody));
        } catch (Exception e) {
            log.error("Error sending payment successful event to SQS: {}", e.getMessage());
        }
    }
}
