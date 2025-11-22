package com.example.payment_service.specification;

import com.example.payment_service.models.PaymentRequestObject;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.models.TransactionStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class TransactionSpecification {
    private TransactionSpecification() {
        throw new IllegalStateException("Utility class");
    }

    // Keyword search across Transaction + PaymentRequestObject
    public static Specification<Transaction> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";
            Join<Transaction, PaymentRequestObject> join = root.join("paymentRequestObject", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("reference")), likePattern),
                    cb.like(cb.lower(root.get("status").as(String.class)), likePattern),
                    cb.like(cb.lower(join.get("email")), likePattern),
                    cb.like(cb.lower(join.get("fullName")), likePattern),
                    cb.like(cb.lower(join.get("organizer")), likePattern),
                    cb.like(cb.lower(join.get("eventTitle")), likePattern)
            );
        };
    }

    // Single status filter
    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }
}
