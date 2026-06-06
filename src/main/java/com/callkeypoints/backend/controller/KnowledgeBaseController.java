package com.callkeypoints.backend.controller;

import com.callkeypoints.backend.model.dto.KnowledgeBaseRequest;
import com.callkeypoints.backend.model.dto.KnowledgeBaseResponse;
import com.callkeypoints.backend.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/knowledge-base")
@Validated
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping
    public KnowledgeBaseResponse get(Authentication auth) {
        return knowledgeBaseService.get(userId(auth));
    }

    @PutMapping
    public KnowledgeBaseResponse put(@Valid @RequestBody KnowledgeBaseRequest request, Authentication auth) {
        return knowledgeBaseService.upsert(userId(auth), request);
    }

    private UUID userId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
