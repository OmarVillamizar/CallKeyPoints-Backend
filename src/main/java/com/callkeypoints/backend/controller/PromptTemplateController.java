package com.callkeypoints.backend.controller;

import com.callkeypoints.backend.model.dto.PromptTemplateRequest;
import com.callkeypoints.backend.model.dto.PromptTemplateResponse;
import com.callkeypoints.backend.service.PromptTemplateService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prompt")
@Validated
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;

    public PromptTemplateController(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    @GetMapping
    public PromptTemplateResponse get(Authentication auth) {
        return promptTemplateService.get(userId(auth));
    }

    @PutMapping
    public PromptTemplateResponse put(@Valid @RequestBody PromptTemplateRequest request, Authentication auth) {
        return promptTemplateService.upsert(userId(auth), request);
    }

    private UUID userId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
