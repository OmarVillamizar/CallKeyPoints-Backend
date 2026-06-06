package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.dto.TechnicianProfileRequest;
import com.callkeypoints.backend.model.dto.TechnicianProfileResponse;

import java.util.UUID;

public interface TechnicianProfileService {

    /** Current user's profile for the management UI. Empty display name if never set. */
    TechnicianProfileResponse get(UUID userId);

    /** Create or replace the current user's display name. */
    TechnicianProfileResponse upsert(UUID userId, TechnicianProfileRequest request);

    /** Display name for call snapshotting. null if never set. */
    String getDisplayName(UUID userId);
}
