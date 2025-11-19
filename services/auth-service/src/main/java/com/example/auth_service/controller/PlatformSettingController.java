package com.example.auth_service.controller;

import com.example.auth_service.dto.request.PlatformSecuritySettingRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.PlatformSecuritySettingResponse;
import com.example.auth_service.service.PlatformSettingService;
import com.example.common_libraries.dto.CustomApiResponse;
import com.example.common_libraries.dto.PlatformNotificationSettingDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/platform-settings")
public class PlatformSettingController {

    private final PlatformSettingService platformSettingService;

    @GetMapping("/security")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<PlatformSecuritySettingResponse>> getSecuritySetting(){
        PlatformSecuritySettingResponse response = platformSettingService.getPlatformSecuritySettings();
        CustomApiResponse<PlatformSecuritySettingResponse> apiResponse = new CustomApiResponse<>("Platform security settings retrieved successfully", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/security")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<Object>> updateSecuritySetting(@Valid @RequestBody PlatformSecuritySettingRequest request){
        platformSettingService.updatePlatformSecuritySetting(request);
        return ResponseEntity.ok(CustomApiResponse.success("Setting updated successfully"));
    }

    @GetMapping("/notifications")
    public ResponseEntity<CustomApiResponse<PlatformNotificationSettingDto>> getNotificationSetting(){
        PlatformNotificationSettingDto response = platformSettingService.getPlatformNotificationSettings();
        CustomApiResponse<PlatformNotificationSettingDto> apiResponse = new CustomApiResponse<>("Platform notification settings retrieved successfully", response);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<Object>> updateNotificationSetting(@Valid @RequestBody PlatformNotificationSettingDto request){
        platformSettingService.updatePlatformNotificationSetting(request);
        return ResponseEntity.ok(CustomApiResponse.success("Setting updated successfully"));
    }

    @GetMapping("/team-members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomApiResponse<List<AuthResponse>>> getTeamMembers(){
        List<AuthResponse> teamMembers = platformSettingService.getTeamMembers();
        CustomApiResponse<List<AuthResponse>> apiResponse = new CustomApiResponse<>("Team members retrieved successfully", teamMembers);
        return ResponseEntity.ok(apiResponse);
    }

}
