package com.event_service.event_service.repositories;


import com.event_service.event_service.models.EventMeetingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventMeetingTypeRepository extends JpaRepository<EventMeetingType, Long> {
}
