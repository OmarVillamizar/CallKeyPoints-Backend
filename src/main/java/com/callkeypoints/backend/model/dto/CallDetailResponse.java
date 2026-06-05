package com.callkeypoints.backend.model.dto;

import java.time.Instant;
import java.util.UUID;

public record CallDetailResponse(
        Long id,
        UUID userId,
        String title,
        String transcript,
        String knowledgeBase,
        String reportExtractedData,
        String reportSolution,
        String reportSummary,
        Instant reportGeneratedAt,
        Instant createdAt,
        Instant updatedAt
) {}
