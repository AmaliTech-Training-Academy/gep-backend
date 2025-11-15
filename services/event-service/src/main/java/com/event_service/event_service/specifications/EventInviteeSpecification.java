package com.event_service.event_service.specifications;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventInvitee;
import com.event_service.event_service.models.enums.InviteeRole;
import org.springframework.data.jpa.domain.Specification;


public class EventInviteeSpecification {
    private EventInviteeSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<EventInvitee> belongsToEvent(Event event) {
        return (root, query, cb) -> cb.equal(root.get("invitation").get("event"), event);
    }

    public static Specification<EventInvitee> hasKeyword(String keyword){
        return (root, query, cb) -> {
            if(keyword == null || keyword.trim().isEmpty()) return cb.conjunction();

            String likePattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("inviteeName")), likePattern),
                    cb.like(cb.lower(root.get("inviteeEmail")), likePattern)
            );
        };
    }

    public static Specification<EventInvitee> hasRole(InviteeRole role){
        if(role == null) return null;
        return (root, query, cb) -> cb.equal(root.get("role"), role.toString());
    }
}
