package com.event_service.event_service.models;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Types;
import java.time.Instant;
import java.util.HashSet;
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

    @Column(name = "user_id", nullable = false)
    private Long userId;

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


}
