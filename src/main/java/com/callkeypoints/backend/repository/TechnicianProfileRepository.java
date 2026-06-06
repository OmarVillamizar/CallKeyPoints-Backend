package com.callkeypoints.backend.repository;

import com.callkeypoints.backend.model.TechnicianProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Long> {
    Optional<TechnicianProfile> findByUserId(UUID userId);
}
