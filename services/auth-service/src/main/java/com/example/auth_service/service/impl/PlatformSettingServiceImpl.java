package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.PlatformSecuritySettingRequest;
import com.example.auth_service.dto.response.PlatformNotificationSettingDto;
import com.example.auth_service.dto.response.PlatformSecuritySettingResponse;
import com.example.auth_service.mapper.PlatformSettingMapper;
import com.example.auth_service.model.PlatformNotificationSetting;
import com.example.auth_service.model.PlatformSecuritySetting;
import com.example.auth_service.repository.PlatformNotificationSettingRepository;
import com.example.auth_service.repository.PlatformSecuritySettingRepository;
import com.example.auth_service.service.PlatformSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PlatformSettingServiceImpl implements PlatformSettingService {

    private final PlatformSecuritySettingRepository platformSecuritySettingRepository;
    private final PlatformNotificationSettingRepository platformNotificationSettingRepository;

    @Override
    public PlatformSecuritySettingResponse getPlatformSecuritySettings() {
        PlatformSecuritySetting platformSecuritySetting = platformSecuritySettingRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Platform security setting not found"));
        return PlatformSettingMapper.toSecuritySettingResponse(platformSecuritySetting);
    }

    @Override
    public void updatePlatformSecuritySetting(PlatformSecuritySettingRequest request) {
        PlatformSecuritySetting platformSecuritySetting = platformSecuritySettingRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Platform security setting not found"));
        if(request.platformName() != null){
            platformSecuritySetting.setPlatformName(request.platformName());
        }
        if(request.platformUrl() != null){
            platformSecuritySetting.setPlatformUrl(request.platformUrl());
        }
        if(request.contactEmail() != null){
            platformSecuritySetting.setContactEmail(request.contactEmail());
        }
        if(request.platformDescription() != null){
            platformSecuritySetting.setPlatformDescription(request.platformDescription());
        }
        if(request.maintenanceMode() != null){
            platformSecuritySetting.setMaintenanceMode(request.maintenanceMode());
        }
        platformSecuritySettingRepository.save(platformSecuritySetting);
    }

    @Override
    public PlatformNotificationSettingDto getPlatformNotificationSettings() {
        PlatformNotificationSetting platformNotificationSetting = platformNotificationSettingRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Platform notification setting not found"));
        return PlatformSettingMapper.toNotificationSettingDto(platformNotificationSetting);
    }

    public void updatePlatformNotificationSetting(PlatformNotificationSettingDto request) {
        PlatformNotificationSetting platformNotificationSetting = platformNotificationSettingRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Platform notification setting not found"));
        if(request.eventCreation() != null){
            platformNotificationSetting.setEventCreation(request.eventCreation());
        }
        if(request.paymentFailures() != null){
            platformNotificationSetting.setPaymentFailures(request.paymentFailures());
        }
        if(request.platformErrors() != null){
            platformNotificationSetting.setPlatformErrors(request.platformErrors());
        }
        platformNotificationSettingRepository.save(platformNotificationSetting);
    }
}
