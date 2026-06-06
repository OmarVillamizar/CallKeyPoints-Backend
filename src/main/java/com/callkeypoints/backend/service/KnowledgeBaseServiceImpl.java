package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.KnowledgeBase;
import com.callkeypoints.backend.model.dto.KnowledgeBaseRequest;
import com.callkeypoints.backend.model.dto.KnowledgeBaseResponse;
import com.callkeypoints.backend.repository.KnowledgeBaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseRepository repository;

    public KnowledgeBaseServiceImpl(KnowledgeBaseRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeBaseResponse get(UUID userId) {
        return repository.findByUserId(userId)
                .map(kb -> new KnowledgeBaseResponse(kb.getContent(), kb.getUpdatedAt()))
                .orElse(new KnowledgeBaseResponse("", null));
    }

    @Override
    public KnowledgeBaseResponse upsert(UUID userId, KnowledgeBaseRequest request) {
        KnowledgeBase kb = repository.findByUserId(userId).orElseGet(() -> {
            KnowledgeBase fresh = new KnowledgeBase();
            fresh.setUserId(userId);
            return fresh;
        });
        kb.setContent(request.content());
        KnowledgeBase saved = repository.save(kb);
        return new KnowledgeBaseResponse(saved.getContent(), saved.getUpdatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public String getContent(UUID userId) {
        return repository.findByUserId(userId)
                .map(KnowledgeBase::getContent)
                .orElse("");
    }
}
