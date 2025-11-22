package com.event_service.event_service.models;

import com.example.common_libraries.enums.WithdrawalMethod;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "withdrawal_requests")
@EntityListeners(AuditingEntityListener.class)
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, name = "withdrawal_method")
    private WithdrawalMethod withdrawalMethod;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name= "provider_name", nullable = false)
    private String providerName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name ="updated_at", nullable = false)
    private LocalDateTime updatedAt;
}