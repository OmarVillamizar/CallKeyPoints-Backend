package com.callkeypoints.backend.controller;

import com.callkeypoints.backend.model.dto.TechnicianProfileRequest;
import com.callkeypoints.backend.model.dto.TechnicianProfileResponse;
import com.callkeypoints.backend.service.TechnicianProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@Validated
public class TechnicianProfileController {

    private final TechnicianProfileService technicianProfileService;

    public TechnicianProfileController(TechnicianProfileService technicianProfileService) {
        this.technicianProfileService = technicianProfileService;
    }

    @GetMapping
    public TechnicianProfileResponse get(Authentication auth) {
        return technicianProfileService.get(userId(auth));
    }

    @PutMapping
    public TechnicianProfileResponse put(@Valid @RequestBody TechnicianProfileRequest request, Authentication auth) {
        return technicianProfileService.upsert(userId(auth), request);
    }

    private UUID userId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
