package com.event_service.event_service.specifications;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventOptions;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class EventSpecification {

    private EventSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Event> hasTitle(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }


    public static Specification<Event> isOn(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();

            ZoneId zone = ZoneId.systemDefault();
            Instant start = date.atStartOfDay(zone).toInstant();
            Instant end = date.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant();

            Predicate matchEventTime = cb.between(root.get("eventTime"), start, end);
            Predicate matchStartTime = cb.between(root.get("startTime"), start, end);

            return cb.or(matchEventTime, matchStartTime);
        };
    }

    public static Specification<Event> isPast() {
        return (root, query, cb) -> {
            Instant now = Instant.now();
            Predicate eventTimePast = cb.lessThan(root.get("eventTime"), now);
            Predicate startTimePast = cb.lessThan(root.get("startTime"), now);
            return cb.or(eventTimePast, startTimePast);
        };
    }

    public static Specification<Event> isUpcoming() {
        return (root, query, cb) -> {
            Instant now = Instant.now();
            Predicate eventTimeFuture = cb.greaterThan(root.get("eventTime"), now);
            Predicate startTimeFuture = cb.greaterThan(root.get("startTime"), now);
            return cb.or(eventTimeFuture, startTimeFuture);
        };
    }


    public static Specification<Event> byLocation(String location) {
        return (root, query, cb) -> {
            if (location == null || location.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
        };
    }


    public static Specification<Event> byPriceFilter(String filter) {
        return (root, query, cb) -> {
            if (filter == null || filter.isBlank()) return cb.conjunction();

            String f = filter.trim().toLowerCase();
            if (f.equals("all") || f.isEmpty()) return cb.conjunction();

            Join<Event, EventOptions> opt = root.join("eventOptions", JoinType.LEFT);

            if (f.equals("free")) return cb.equal(opt.get("ticketPrice"), BigDecimal.ZERO);
            if (f.equals("paid")) return cb.greaterThan(opt.get("ticketPrice"), BigDecimal.ZERO);

            return cb.conjunction();
        };
    }
}
