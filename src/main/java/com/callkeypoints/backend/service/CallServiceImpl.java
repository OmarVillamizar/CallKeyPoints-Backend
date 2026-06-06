package com.callkeypoints.backend.service;

import com.callkeypoints.backend.exception.ResourceNotFoundException;
import com.callkeypoints.backend.model.Call;
import com.callkeypoints.backend.model.dto.*;
import com.callkeypoints.backend.repository.CallRepository;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CallServiceImpl implements CallService {

    private final CallRepository callRepository;
    private final DeepSeekService deepSeekService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final TechnicianProfileService technicianProfileService;
    private final ObjectMapper objectMapper;

    public CallServiceImpl(CallRepository callRepository, DeepSeekService deepSeekService,
                           KnowledgeBaseService knowledgeBaseService,
                           TechnicianProfileService technicianProfileService, ObjectMapper objectMapper) {
        this.callRepository = callRepository;
        this.deepSeekService = deepSeekService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.technicianProfileService = technicianProfileService;
        this.objectMapper = objectMapper;
    }

    @Override
    public CallDetailResponse createCall(CallRequest request, UUID userId) {
        String knowledgeBase = knowledgeBaseService.getContent(userId);
        DeepSeekExtractedData extracted = deepSeekService.extract(request.transcript(), knowledgeBase);

        String reportJson;
        try {
            reportJson = objectMapper.writeValueAsString(extracted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize extracted data", e);
        }

        Call call = new Call();
        call.setUserId(userId);
        call.setTechnicianName(technicianProfileService.getDisplayName(userId));
        call.setTranscript(request.transcript());
        call.setKnowledgeBase(knowledgeBase);
        call.setTitle(buildTitle(extracted, request.transcript()));
        call.setReportExtractedData(reportJson);
        call.setReportGeneratedAt(Instant.now());

        // Explicit columns for querying/display
        call.setAtendio(extracted.atendio());
        call.setNumeroCuenta(extracted.numeroCuenta());
        call.setDireccion(extracted.direccion());
        call.setProtocoloKb(extracted.protocoloKb());
        call.setSeveridad(extracted.severidad());
        call.setResponsabilidad(extracted.responsabilidad());
        call.setSintomaReportado(extracted.sintomaReportado());
        call.setDiagnostico(extracted.diagnostico());
        call.setAccionesRecomendadas(extracted.accionesRecomendadas());
        call.setEstadoResolucion(extracted.estadoResolucion());
        call.setOrdenTrabajo(extracted.ordenTrabajo());
        call.setTiempoRespuesta(extracted.tiempoRespuesta());
        call.setCumplimientoProtocolo(extracted.cumplimientoProtocolo());
        call.setSentimientoCliente(extracted.sentimientoCliente());
        call.setReportSummary(extracted.resumen());

        return toDetailResponse(callRepository.save(call));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CallSummaryResponse> listCalls(UUID userId) {
        return callRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(c -> new CallSummaryResponse(c.getId(), c.getTitle(), c.getCreatedAt()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CallDetailResponse getCall(Long id, UUID userId) {
        Call call = callRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found"));
        return toDetailResponse(call);
    }

    @Override
    public void deleteCall(Long id, UUID userId) {
        Call call = callRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found"));
        callRepository.delete(call);
    }

    private String buildTitle(DeepSeekExtractedData extracted, String transcript) {
        String source = (extracted.cliente() != null && !extracted.cliente().isBlank())
                ? extracted.cliente()
                : transcript;
        return source.length() > 80 ? source.substring(0, 80) : source;
    }

    private CallDetailResponse toDetailResponse(Call call) {
        return new CallDetailResponse(
                call.getId(),
                call.getUserId(),
                call.getTitle(),
                call.getTechnicianName(),
                call.getTranscript(),
                call.getKnowledgeBase(),
                call.getReportExtractedData(),
                call.getAtendio(),
                call.getNumeroCuenta(),
                call.getDireccion(),
                call.getProtocoloKb(),
                call.getSeveridad(),
                call.getResponsabilidad(),
                call.getSintomaReportado(),
                call.getDiagnostico(),
                call.getAccionesRecomendadas(),
                call.getEstadoResolucion(),
                call.getOrdenTrabajo(),
                call.getTiempoRespuesta(),
                call.getCumplimientoProtocolo(),
                call.getSentimientoCliente(),
                call.getReportSummary(),
                call.getReportGeneratedAt(),
                call.getCreatedAt(),
                call.getUpdatedAt()
        );
    }
}
