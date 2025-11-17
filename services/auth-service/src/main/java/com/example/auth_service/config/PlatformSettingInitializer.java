package com.example.auth_service.config;

import com.example.auth_service.model.PlatformSecuritySetting;
import com.example.auth_service.repository.PlatformSecuritySettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformSettingInitializer {

    private final PlatformSecuritySettingRepository platformSecuritySettingRepository;

    @PostConstruct
    @Transactional
    public void init(){
        createDefaultSettings();
    }

    private void createDefaultSettings(){
        if(platformSecuritySettingRepository.findById(1L).isEmpty()){
            PlatformSecuritySetting defaultSecuritySetting = PlatformSecuritySetting.builder()
                    .platformName("Event Hub")
                    .platformUrl("https://events.sankofagrid.com")
                    .contactEmail("support@eventhub.com")
                    .platformDescription("EventHub is a comprehensive event management platform for organizers and attendees")
                    .maintenanceMode(false)
                    .build();
            platformSecuritySettingRepository.save(defaultSecuritySetting);
        }
    }
}
