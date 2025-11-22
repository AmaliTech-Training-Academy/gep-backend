package com.example.payment_service.repos;

import com.example.payment_service.models.PaymentRequestObject;
import com.example.payment_service.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRequestObjectRepository extends JpaRepository<PaymentRequestObject, Long> {

    Optional<PaymentRequestObject> findByTransaction(Transaction transaction);

    void deleteByTransaction(Transaction transaction);
}
