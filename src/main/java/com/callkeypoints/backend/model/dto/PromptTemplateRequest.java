package com.callkeypoints.backend.model.dto;

import jakarta.validation.constraints.NotNull;

public record PromptTemplateRequest(
        @NotNull String content
) {}
