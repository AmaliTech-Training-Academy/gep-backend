package com.event_service.event_service.repositories;

import com.event_service.event_service.models.Event;
import com.event_service.event_service.models.EventImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventImagesRepository extends JpaRepository<EventImages, Long> {
    @Query("SELECT ei.image FROM EventImages ei WHERE ei.event = :event")
    List<String> findImageUrlsByEvent(@Param("event") Event event);
}
