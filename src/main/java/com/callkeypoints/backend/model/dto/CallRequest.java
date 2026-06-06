package com.callkeypoints.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CallRequest(
        @NotBlank @Size(max = 50000, message = "transcript exceeds the 50000 character limit")
        String transcript
) {}
