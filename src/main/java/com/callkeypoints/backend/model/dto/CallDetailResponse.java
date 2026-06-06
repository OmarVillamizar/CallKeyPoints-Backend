package com.callkeypoints.backend.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CallDetailResponse(
        Long id,
        UUID userId,
        String title,
        String technicianName,
        String transcript,
        String knowledgeBase,
        String reportExtractedData,
        // extracted fields (explicit columns)
        String atendio,
        String numeroCuenta,
        String direccion,
        String protocoloKb,
        String severidad,
        String responsabilidad,
        String sintomaReportado,
        String diagnostico,
        List<String> accionesRecomendadas,
        String estadoResolucion,
        String ordenTrabajo,
        String tiempoRespuesta,
        String cumplimientoProtocolo,
        String sentimientoCliente,
        String reportSummary,
        Instant reportGeneratedAt,
        Instant createdAt,
        Instant updatedAt
) {}
