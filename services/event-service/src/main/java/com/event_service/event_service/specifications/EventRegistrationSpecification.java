package com.event_service.event_service.specifications;

import com.event_service.event_service.models.EventRegistration;
import com.event_service.event_service.models.TicketType;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class EventRegistrationSpecification {
    private EventRegistrationSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<EventRegistration> hasEvent(Long eventId) {
        return (root, query, cb) -> cb.equal(root.get("event").get("id"), eventId);
    }

    public static Specification<EventRegistration> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) return cb.conjunction();

            String likePattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), likePattern),
                    cb.like(cb.lower(root.get("email")), likePattern)
            );
        };
    }

    public static Specification<EventRegistration> hasTicketType(String ticketType) {
        return (root, query, cb) -> {
            if(ticketType == null || ticketType.trim().isEmpty()) return cb.conjunction();

            Join<EventRegistration, TicketType> ticketTypeJoin = root.join("ticketType");
            return cb.equal(
                    cb.lower(ticketTypeJoin.get("type")),
                    ticketType.toLowerCase()
            );
        };
    }
}
