package com.callkeypoints.backend.model.dto;

import jakarta.validation.constraints.NotNull;

public record KnowledgeBaseRequest(
        @NotNull String content
) {}
