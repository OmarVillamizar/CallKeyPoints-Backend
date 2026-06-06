package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.dto.PromptTemplateRequest;
import com.callkeypoints.backend.model.dto.PromptTemplateResponse;

import java.util.UUID;

public interface PromptTemplateService {

    /** Current user's saved prompt for the management UI. Empty content if never set. */
    PromptTemplateResponse get(UUID userId);

    /** Create or replace the current user's prompt. */
    PromptTemplateResponse upsert(UUID userId, PromptTemplateRequest request);

    /** Raw prompt text for prompt assembly. Empty string if never set (caller applies the default). */
    String getContent(UUID userId);
}
