package com.event_service.event_service.utils;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.enums.EventStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;



public class EventManagementSpecifications {
    private EventManagementSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a Specification to filter events by a keyword.
     * The keyword is matched against the event title and organizerName fields (case-insensitive).
     *
     * @param keyword The keyword to search for. If null or empty, no filtering is applied.
     * @return A Specification for filtering events by the given keyword.
     */
    public static Specification<Event> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), likePattern)
//                    cb.like(cb.lower(root.get("organizerName")), likePattern)
            );
        };
    }

    /**
     * Creates a Specification to filter events by their derived status.
     * Status is determined using the following logic:
     *
     * <pre>
     * if (start.isBefore(now) && end.isAfter(now)) {
     *     return EventStatus.ACTIVE;
     * } else if (end.isBefore(now)) {
     *     return EventStatus.COMPLETED;
     * } else if (start.isAfter(now)) {
     *     return EventStatus.DRAFT;
     * } else {
     *     return EventStatus.PENDING;
     * }
     * </pre>
     *
     * @param status The status to filter by. If null, no filtering is applied.
     * @return A Specification for filtering events by their derived status.
     */
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
