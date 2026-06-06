package com.callkeypoints.backend.model.dto;

import jakarta.validation.constraints.NotBlank;

public record TechnicianProfileRequest(
        @NotBlank String displayName
) {}
