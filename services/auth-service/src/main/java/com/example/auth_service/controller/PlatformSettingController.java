package com.example.auth_service.controller;

import com.example.auth_service.dto.request.PlatformSecuritySettingRequest;
import com.example.auth_service.dto.response.PlatformSecuritySettingResponse;
import com.example.auth_service.service.PlatformSettingService;
import com.example.common_libraries.dto.CustomApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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



}
