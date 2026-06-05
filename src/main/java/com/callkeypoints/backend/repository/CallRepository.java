package com.callkeypoints.backend.repository;

import com.callkeypoints.backend.model.Call;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CallRepository extends JpaRepository<Call, Long> {
    List<Call> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Call> findByIdAndUserId(Long id, UUID userId);
}
