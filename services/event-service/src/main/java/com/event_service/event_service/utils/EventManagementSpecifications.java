package com.event_service.event_service.utils;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.enums.EventStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;



public class EventManagementSpecifications {
    private EventManagementSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Event> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), likePattern),
                    cb.like(cb.lower(root.get("createdBy")), likePattern)
            );
        };
    }


    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }

            Instant now = Instant.now();

            return switch (status) {
                case ACTIVE -> cb.and(
                        cb.lessThan(root.get("startTime"), now),
                        cb.greaterThan(root.get("endTime"), now)
                );
                case COMPLETED -> cb.lessThan(root.get("endTime"), now);
                case DRAFT -> cb.greaterThan(root.get("startTime"), now);
                case PENDING -> cb.or(
                        cb.isNull(root.get("startTime")),
                        cb.isNull(root.get("endTime"))
                );
            };
        };
    }
}
