package com.callkeypoints.backend.model.dto;

import java.time.Instant;

public record CallSummaryResponse(
        Long id,
        String title,
        Instant createdAt
) {}
