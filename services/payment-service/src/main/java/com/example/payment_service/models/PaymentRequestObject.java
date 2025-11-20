package com.example.payment_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Entity
@Table(name = "payment_request")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class PaymentRequestObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Payment details
    private String email;

    private String fullName;

    private Double amount;

    private Long ticketTypeId;

    private Long numberOfTickets;

    // Event details
    private Long eventId;

    private String eventTitle;

    private String location;

    private String organizer;

    private Instant startDate;

    @OneToOne(cascade = CascadeType.ALL)
    private Transaction transaction;

    // Timestamps
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
