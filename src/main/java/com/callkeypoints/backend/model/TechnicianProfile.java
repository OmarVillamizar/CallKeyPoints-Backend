package com.callkeypoints.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * The authenticated technician's display identity. One row per user; the display name is
 * snapshotted onto every call so the report records who handled it.
 */
@Entity
@Table(name = "technician_profiles", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class TechnicianProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "display_name", nullable = false, columnDefinition = "TEXT")
    private String displayName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
