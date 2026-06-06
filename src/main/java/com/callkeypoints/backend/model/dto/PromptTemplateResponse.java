package com.callkeypoints.backend.model.dto;

import java.time.Instant;

public record PromptTemplateResponse(
        String content,
        Instant updatedAt
) {}
