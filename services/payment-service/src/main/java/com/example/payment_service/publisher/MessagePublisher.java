package com.example.payment_service.publisher;

import com.example.common_libraries.dto.queue_events.PaymentStatusEvent;
import com.example.common_libraries.dto.queue_events.ProcessPaymentEvent;

public interface MessagePublisher {
    void publishPaymentStatusToQueue(PaymentStatusEvent statusEvent);

    void publishPaymentSuccessfulEventToQueue(ProcessPaymentEvent event);
}
