package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.dto.KnowledgeBaseRequest;
import com.callkeypoints.backend.model.dto.KnowledgeBaseResponse;

import java.util.UUID;

public interface KnowledgeBaseService {

    /** Current user's KB for the management UI. Empty content if never set. */
    KnowledgeBaseResponse get(UUID userId);

    /** Create or replace the current user's KB. */
    KnowledgeBaseResponse upsert(UUID userId, KnowledgeBaseRequest request);

    /** Raw KB text for prompt assembly. Empty string if never set. */
    String getContent(UUID userId);
}
