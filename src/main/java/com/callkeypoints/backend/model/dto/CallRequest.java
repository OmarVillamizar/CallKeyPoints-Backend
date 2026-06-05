package com.callkeypoints.backend.model.dto;

import jakarta.validation.constraints.NotBlank;

public record CallRequest(
        @NotBlank String transcript,
        String knowledgeBase
) {}
