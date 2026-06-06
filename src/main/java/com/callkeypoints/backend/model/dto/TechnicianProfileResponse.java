package com.callkeypoints.backend.model.dto;

import java.time.Instant;

public record TechnicianProfileResponse(
        String displayName,
        Instant updatedAt
) {}
