package com.callkeypoints.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "calls", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    /** Display name of the authenticated system user who processed the call. */
    @Column(name = "technician_name", columnDefinition = "TEXT")
    private String technicianName;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String transcript;

    @Column(name = "knowledge_base", columnDefinition = "TEXT")
    private String knowledgeBase;

    // ── Full JSON snapshot of the extracted report ─────────────────────────────
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_extracted_data", columnDefinition = "jsonb")
    private String reportExtractedData;

    // ── Explicit indexed columns for querying/display ──────────────────────────

    /** Agent name as spoken in the transcript (extracted by LLM). */
    @Column(name = "atendio", columnDefinition = "TEXT")
    private String atendio;

    @Column(name = "numero_cuenta", columnDefinition = "TEXT")
    private String numeroCuenta;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "protocolo_kb", columnDefinition = "TEXT")
    private String protocoloKb;

    @Column(name = "severidad", length = 20)
    private String severidad;

    @Column(name = "responsabilidad", length = 30)
    private String responsabilidad;

    @Column(name = "sintoma_reportado", columnDefinition = "TEXT")
    private String sintomaReportado;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "acciones_recomendadas", columnDefinition = "jsonb")
    private List<String> accionesRecomendadas;

    @Column(name = "estado_resolucion", length = 40)
    private String estadoResolucion;

    @Column(name = "orden_trabajo", columnDefinition = "TEXT")
    private String ordenTrabajo;

    @Column(name = "tiempo_respuesta", columnDefinition = "TEXT")
    private String tiempoRespuesta;

    @Column(name = "cumplimiento_protocolo", columnDefinition = "TEXT")
    private String cumplimientoProtocolo;

    @Column(name = "sentimiento_cliente", length = 20)
    private String sentimientoCliente;

    @Column(name = "report_summary", columnDefinition = "TEXT")
    private String reportSummary;

    @Column(name = "report_generated_at")
    private Instant reportGeneratedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
