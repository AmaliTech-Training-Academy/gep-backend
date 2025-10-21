package com.event_service.event_service.models;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;


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
    private String description;

    @Column(nullable = false)
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
    private Instant endTimeZoneId;

    @Column
    private Instant eventTime;

    @Column
    private Instant eventTimeZoneId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "event_type_id", referencedColumnName = "id")
    private EventType eventType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "event_meeting_type_id", referencedColumnName = "id")
    private EventMeetingType eventMeetingType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "event_option_id", referencedColumnName = "id")
    private EventOptions eventOptions;

    @CreatedDate
    @Column(name = "created_at", nullable =false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;


}
