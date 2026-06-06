package com.callkeypoints.backend.model.dto;

import java.time.Instant;

public record KnowledgeBaseResponse(
        String content,
        Instant updatedAt
) {}
