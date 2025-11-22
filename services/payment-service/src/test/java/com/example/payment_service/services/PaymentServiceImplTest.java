package com.example.payment_service.services;

import com.example.payment_service.dto.TransactionResponse;
import com.example.payment_service.models.PaymentRequestObject;
import com.example.payment_service.models.Transaction;
import com.example.payment_service.models.TransactionStatus;
import com.example.payment_service.repos.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Transaction transaction1;
    private Transaction transaction2;
    private Transaction transaction3;
    private PaymentRequestObject paymentRequestObject1;
    private PaymentRequestObject paymentRequestObject2;
    private PaymentRequestObject paymentRequestObject3;

    @BeforeEach
    void setup() {
        Instant now = Instant.now();

        // Setup PaymentRequestObject 1 - Complete data
        paymentRequestObject1 = PaymentRequestObject.builder()
                .id(1L)
                .email("john.doe@example.com")
                .fullName("John Doe")
                .amount(100.50)
                .ticketTypeId(1L)
                .numberOfTickets(2L)
                .eventId(10L)
                .eventTitle("Tech Conference 2024")
                .location("New York")
                .organizer("TechCorp")
                .startDate(now.plus(30, ChronoUnit.DAYS))
                .createdAt(now.minus(1, ChronoUnit.HOURS))
                .updatedAt(now.minus(1, ChronoUnit.HOURS))
                .build();

        // Setup PaymentRequestObject 2 - Partial data (some null fields)
        paymentRequestObject2 = PaymentRequestObject.builder()
                .id(2L)
                .email("jane.smith@example.com")
                .fullName("Jane Smith")
                .amount(200.00)
                .eventTitle(null)  // Null event title
                .organizer(null)   // Null organizer
                .createdAt(now.minus(2, ChronoUnit.HOURS))
                .updatedAt(now.minus(2, ChronoUnit.HOURS))
                .build();

        // Setup PaymentRequestObject 3 - Null payment request object (edge case)
        paymentRequestObject3 = null;

        // Setup Transaction 1 - SUCCESSFUL with complete data
        transaction1 = Transaction.builder()
                .id(1L)
                .accessToken("access_token_1")
                .amount(new BigDecimal("100.50"))
                .reference("TXN-001")
                .authorizationUrl("https://paystack.com/auth/1")
                .email("john.doe@example.com")
                .status(TransactionStatus.SUCCESSFUL)
                .paymentRequestObject(paymentRequestObject1)
                .createdAt(now.minus(1, ChronoUnit.HOURS))
                .updatedAt(now.minus(1, ChronoUnit.HOURS))
                .build();

        // Setup Transaction 2 - PENDING with partial data
        transaction2 = Transaction.builder()
                .id(2L)
                .accessToken("access_token_2")
                .amount(new BigDecimal("200.00"))
                .reference("TXN-002")
                .authorizationUrl("https://paystack.com/auth/2")
                .email("jane.smith@example.com")
                .status(TransactionStatus.PENDING)
                .paymentRequestObject(paymentRequestObject2)
                .createdAt(now.minus(2, ChronoUnit.HOURS))
                .updatedAt(now.minus(2, ChronoUnit.HOURS))
                .build();

        // Setup Transaction 3 - FAILED with null payment request object
        transaction3 = Transaction.builder()
                .id(3L)
                .accessToken("access_token_3")
                .amount(new BigDecimal("50.00"))
                .reference("TXN-003")
                .authorizationUrl("https://paystack.com/auth/3")
                .email("test@example.com")
                .status(TransactionStatus.FAILED)
                .paymentRequestObject(null)
                .createdAt(now.minus(3, ChronoUnit.HOURS))
                .updatedAt(now.minus(3, ChronoUnit.HOURS))
                .build();

        // Set bidirectional relationships
        paymentRequestObject1.setTransaction(transaction1);
        paymentRequestObject2.setTransaction(transaction2);
    }

    // ----------------------------------------------------------------------
    // getAllTransactions()
    // ----------------------------------------------------------------------
    @Nested
    @DisplayName("getAllTransactions()")
    class GetAllTransactionsTests {

        @Test
        @DisplayName("should return all transactions with default page 0, no filters")
        void shouldReturnAllTransactionsWithDefaultPage() {
            // Arrange
            List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), transactions.size());

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.getTotalElements());
            assertEquals(3, result.getContent().size());

            // Verify first transaction
            TransactionResponse response1 = result.getContent().get(0);
            assertEquals("TXN-001", response1.transactionId());
            assertEquals("Tech Conference 2024", response1.eventName());
            assertEquals("TechCorp", response1.eventOrganizer());
            assertEquals("john.doe@example.com", response1.attendeeEmail());
            assertEquals(new BigDecimal("100.50"), response1.amount());
            assertEquals("N/A", response1.paymentMethod());
            assertEquals(TransactionStatus.SUCCESSFUL, response1.status());
            assertNotNull(response1.transactionTime());

            // Verify repository call
            ArgumentCaptor<Specification<Transaction>> specCaptor = ArgumentCaptor.forClass(Specification.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(transactionRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertEquals(0, pageable.getPageNumber());
            assertEquals(10, pageable.getPageSize());
            assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("createdAt").getDirection());
        }

        @Test
        @DisplayName("should return transactions with N/A for null payment request object fields")
        void shouldHandleNullPaymentRequestObject() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction3);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            TransactionResponse response = result.getContent().get(0);
            assertEquals("TXN-003", response.transactionId());
            assertEquals("N/A", response.eventName());
            assertEquals("N/A", response.eventOrganizer());
            assertEquals("N/A", response.attendeeEmail());
            assertEquals(new BigDecimal("50.00"), response.amount());
            assertEquals(TransactionStatus.FAILED, response.status());
        }

        @Test
        @DisplayName("should return transactions with N/A for null fields in payment request object")
        void shouldHandleNullFieldsInPaymentRequestObject() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction2);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());

            TransactionResponse response = result.getContent().get(0);
            assertEquals("TXN-002", response.transactionId());
            assertEquals("N/A", response.eventName());  // null eventTitle
            assertEquals("N/A", response.eventOrganizer());  // null organizer
            assertEquals("jane.smith@example.com", response.attendeeEmail());
            assertEquals(new BigDecimal("200.00"), response.amount());
            assertEquals(TransactionStatus.PENDING, response.status());
        }

        @Test
        @DisplayName("should filter transactions by keyword")
        void shouldFilterTransactionsByKeyword() {
            // Arrange
            String keyword = "john";
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, keyword, null);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("TXN-001", result.getContent().get(0).transactionId());

            verify(transactionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("should filter transactions by status SUCCESSFUL")
        void shouldFilterTransactionsByStatusSuccessful() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, TransactionStatus.SUCCESSFUL);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(TransactionStatus.SUCCESSFUL, result.getContent().get(0).status());

            verify(transactionRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("should filter transactions by status PENDING")
        void shouldFilterTransactionsByStatusPending() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction2);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, TransactionStatus.PENDING);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(TransactionStatus.PENDING, result.getContent().get(0).status());
        }

        @Test
        @DisplayName("should filter transactions by status FAILED")
        void shouldFilterTransactionsByStatusFailed() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction3);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, TransactionStatus.FAILED);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(TransactionStatus.FAILED, result.getContent().get(0).status());
        }

        @Test
        @DisplayName("should filter transactions by both keyword and status")
        void shouldFilterTransactionsByKeywordAndStatus() {
            // Arrange
            String keyword = "Tech";
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, keyword, TransactionStatus.SUCCESSFUL);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("TXN-001", result.getContent().get(0).transactionId());
            assertEquals(TransactionStatus.SUCCESSFUL, result.getContent().get(0).status());
        }

        @Test
        @DisplayName("should handle empty keyword (blank string)")
        void shouldHandleEmptyKeyword() {
            // Arrange
            List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 3);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, "   ", null);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.getTotalElements());
        }

        @Test
        @DisplayName("should handle whitespace-only keyword")
        void shouldHandleWhitespaceKeyword() {
            // Arrange
            List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 3);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, "\t\n", null);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.getTotalElements());
        }

        @Test
        @DisplayName("should return empty page when no transactions found")
        void shouldReturnEmptyPageWhenNoTransactionsFound() {
            // Arrange
            Page<Transaction> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 0);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }

        @Test
        @DisplayName("should handle negative page number by converting to 0")
        void shouldHandleNegativePageNumber() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(-5, null, null);

            // Assert
            assertNotNull(result);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(transactionRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertEquals(0, pageable.getPageNumber());
        }

        @Test
        @DisplayName("should handle page number 1")
        void shouldHandlePageNumber1() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction2);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 11);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(1, null, null);

            // Assert
            assertNotNull(result);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(transactionRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertEquals(1, pageable.getPageNumber());
        }

        @Test
        @DisplayName("should handle large page number")
        void shouldHandleLargePageNumber() {
            // Arrange
            Page<Transaction> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(999, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 0);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(999, null, null);

            // Assert
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
        }

        @Test
        @DisplayName("should verify page size is always 10")
        void shouldVerifyPageSizeIs10() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            paymentService.getAllTransactions(0, null, null);

            // Assert
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(transactionRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertEquals(10, pageable.getPageSize());
        }

        @Test
        @DisplayName("should verify sorting is DESC by createdAt")
        void shouldVerifySortingIsDescByCreatedAt() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            paymentService.getAllTransactions(0, null, null);

            // Assert
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(transactionRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            Sort.Order order = pageable.getSort().getOrderFor("createdAt");
            assertNotNull(order);
            assertEquals(Sort.Direction.DESC, order.getDirection());
            assertEquals("createdAt", order.getProperty());
        }

        @Test
        @DisplayName("should handle transaction with zero amount")
        void shouldHandleTransactionWithZeroAmount() {
            // Arrange
            transaction1.setAmount(BigDecimal.ZERO);
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.getContent().get(0).amount());
        }

        @Test
        @DisplayName("should handle transaction with null amount")
        void shouldHandleTransactionWithNullAmount() {
            // Arrange
            transaction1.setAmount(null);
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            assertNull(result.getContent().get(0).amount());
        }

        @Test
        @DisplayName("should handle transaction with very large amount")
        void shouldHandleTransactionWithVeryLargeAmount() {
            // Arrange
            transaction1.setAmount(new BigDecimal("999999999.99"));
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(new BigDecimal("999999999.99"), result.getContent().get(0).amount());
        }

        @Test
        @DisplayName("should handle special characters in keyword")
        void shouldHandleSpecialCharactersInKeyword() {
            // Arrange
            String keyword = "test@#$%";
            List<Transaction> transactions = Collections.emptyList();
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 0);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, keyword, null);

            // Assert
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
        }

        @Test
        @DisplayName("should trim keyword before filtering")
        void shouldTrimKeywordBeforeFiltering() {
            // Arrange
            String keyword = "  john  ";
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, keyword, null);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("should handle email with null value in PaymentRequestObject")
        void shouldHandleNullEmailInPaymentRequestObject() {
            // Arrange
            paymentRequestObject1.setEmail(null);
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            assertEquals("N/A", result.getContent().get(0).attendeeEmail());
        }

        @Test
        @DisplayName("should verify TransactionResponse paymentMethod is always N/A")
        void shouldVerifyPaymentMethodIsAlwaysNA() {
            // Arrange
            List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 3);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            result.getContent().forEach(response ->
                    assertEquals("N/A", response.paymentMethod())
            );
        }

        @Test
        @DisplayName("should correctly map all transaction fields to response")
        void shouldCorrectlyMapAllTransactionFieldsToResponse() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            TransactionResponse response = result.getContent().get(0);
            assertEquals(transaction1.getReference(), response.transactionId());
            assertEquals(transaction1.getPaymentRequestObject().getEventTitle(), response.eventName());
            assertEquals(transaction1.getPaymentRequestObject().getOrganizer(), response.eventOrganizer());
            assertEquals(transaction1.getPaymentRequestObject().getEmail(), response.attendeeEmail());
            assertEquals(transaction1.getAmount(), response.amount());
            assertEquals("N/A", response.paymentMethod());
            assertEquals(transaction1.getStatus(), response.status());
            assertEquals(transaction1.getCreatedAt(), response.transactionTime());
        }

        @Test
        @DisplayName("should handle multiple transactions on same page")
        void shouldHandleMultipleTransactionsOnSamePage() {
            // Arrange
            List<Transaction> transactions = Arrays.asList(transaction1, transaction2, transaction3);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 3);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            Page<TransactionResponse> result = paymentService.getAllTransactions(0, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.getContent().size());
            assertEquals(1, result.getTotalPages());
        }

        @Test
        @DisplayName("should verify repository is called exactly once")
        void shouldVerifyRepositoryIsCalledExactlyOnce() {
            // Arrange
            List<Transaction> transactions = Collections.singletonList(transaction1);
            Page<Transaction> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

            when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(transactionPage);

            // Act
            paymentService.getAllTransactions(0, null, null);

            // Assert
            verify(transactionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        }
    }
}