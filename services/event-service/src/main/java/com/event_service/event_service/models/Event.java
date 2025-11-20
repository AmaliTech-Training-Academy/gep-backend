package com.event_service.event_service.models;


import com.event_service.event_service.models.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @JdbcTypeCode(Types.LONGVARCHAR)
    private String description;

    @Column
    private String location;

    @Column(nullable = false)
    private String flyerUrl;

    @Column
    private String zoomMeetingLink;

    @Column
    private Instant startTime;

    @Column
    private String startTimeZoneId;

    @Column
    private Instant endTime;

    @Column
    private String endTimeZoneId;

    @Column
    private Instant eventTime;

    @Column
    private String eventTimeZoneId;

    @Column(nullable = false)
    private String createdBy = "Roger Satsi";

    @Column(nullable = false)
    private Long userId = 1L;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<EventRegistration> eventRegistrations;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TicketType> ticketTypes;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventImages> eventImages = new HashSet<>();

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id")
    private EventType eventType;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_meeting_type_id")
    private EventMeetingType eventMeetingType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "event_options_id")
    private EventOptions eventOptions;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventInvitation> invitations = new HashSet<>();

    public void addImage(EventImages eventImage) {
        this.eventImages.add(eventImage);
        eventImage.setEvent(this);
    }

    @CreatedDate
    @Column(name = "created_at", nullable =false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public EventStatus getStatus() {
        Instant now = Instant.now();
        Instant start = getStartTime();
        Instant end = getEndTime();

        if (start == null) {
            return EventStatus.PENDING;
        }

        if (end == null) {
            end = start.plus(1, ChronoUnit.DAYS);
        }

        if (now.isBefore(start)) {
            return EventStatus.DRAFT;
        } else if (!now.isBefore(start) && !now.isAfter(end)) {
            return EventStatus.ACTIVE;
        } else if (now.isAfter(end)) {
            return EventStatus.COMPLETED;
        }
        return EventStatus.PENDING;
    }

    @PrePersist
    @PreUpdate
    public void ensureEndTime() {
        if (startTime == null && eventTime != null) {
            startTime = eventTime;
        }

        if (endTime == null) {
            endTime = startTime.plus(1, ChronoUnit.DAYS);
        } else {
            long duration = Duration.between(startTime, endTime).toDays();
            if (duration == 1) {
                endTime = startTime.plus(1, ChronoUnit.DAYS);
            }
        }
    }

    public void addTicketType(TicketType ticketType) {
        if (this.ticketTypes == null) {
            this.ticketTypes = new ArrayList<>();
        }
        this.ticketTypes.add(ticketType);
        ticketType.setEvent(this);
    }

}
