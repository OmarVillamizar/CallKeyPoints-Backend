package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.TechnicianProfile;
import com.callkeypoints.backend.model.dto.TechnicianProfileRequest;
import com.callkeypoints.backend.model.dto.TechnicianProfileResponse;
import com.callkeypoints.backend.repository.TechnicianProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class TechnicianProfileServiceImpl implements TechnicianProfileService {

    private final TechnicianProfileRepository repository;

    public TechnicianProfileServiceImpl(TechnicianProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicianProfileResponse get(UUID userId) {
        return repository.findByUserId(userId)
                .map(p -> new TechnicianProfileResponse(p.getDisplayName(), p.getUpdatedAt()))
                .orElse(new TechnicianProfileResponse("", null));
    }

    @Override
    public TechnicianProfileResponse upsert(UUID userId, TechnicianProfileRequest request) {
        TechnicianProfile profile = repository.findByUserId(userId).orElseGet(() -> {
            TechnicianProfile fresh = new TechnicianProfile();
            fresh.setUserId(userId);
            return fresh;
        });
        profile.setDisplayName(request.displayName());
        TechnicianProfile saved = repository.save(profile);
        return new TechnicianProfileResponse(saved.getDisplayName(), saved.getUpdatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public String getDisplayName(UUID userId) {
        return repository.findByUserId(userId)
                .map(TechnicianProfile::getDisplayName)
                .orElse(null);
    }
}
