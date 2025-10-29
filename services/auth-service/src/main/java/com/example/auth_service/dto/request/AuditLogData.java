package com.example.auth_service.dto.request;


import lombok.*;

import java.time.Instant;


@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class AuditLogData {
    private String email;
    private String ipAddress;
    private Instant timestamp;
}
