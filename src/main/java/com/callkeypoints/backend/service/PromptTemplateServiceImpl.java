package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.PromptTemplate;
import com.callkeypoints.backend.model.dto.PromptTemplateRequest;
import com.callkeypoints.backend.model.dto.PromptTemplateResponse;
import com.callkeypoints.backend.repository.PromptTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private final PromptTemplateRepository repository;

    public PromptTemplateServiceImpl(PromptTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public PromptTemplateResponse get(UUID userId) {
        return repository.findByUserId(userId)
                .map(p -> new PromptTemplateResponse(p.getContent(), p.getUpdatedAt()))
                .orElse(new PromptTemplateResponse("", null));
    }

    @Override
    public PromptTemplateResponse upsert(UUID userId, PromptTemplateRequest request) {
        PromptTemplate prompt = repository.findByUserId(userId).orElseGet(() -> {
            PromptTemplate fresh = new PromptTemplate();
            fresh.setUserId(userId);
            return fresh;
        });
        prompt.setContent(request.content());
        PromptTemplate saved = repository.save(prompt);
        return new PromptTemplateResponse(saved.getContent(), saved.getUpdatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public String getContent(UUID userId) {
        return repository.findByUserId(userId)
                .map(PromptTemplate::getContent)
                .orElse("");
    }
}
