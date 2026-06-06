package com.callkeypoints.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ExtractedReport(
        String cliente,
        String atendio,
        @JsonProperty("numero_cuenta")          String numeroCuenta,
        String direccion,
        @JsonProperty("protocolo_kb")           String protocoloKb,
        String severidad,
        String responsabilidad,
        @JsonProperty("sintoma_reportado")      String sintomaReportado,
        String diagnostico,
        @JsonProperty("acciones_recomendadas")  List<String> accionesRecomendadas,
        @JsonProperty("estado_resolucion")      String estadoResolucion,
        @JsonProperty("orden_trabajo")          String ordenTrabajo,
        @JsonProperty("tiempo_respuesta")       String tiempoRespuesta,
        @JsonProperty("cumplimiento_protocolo") String cumplimientoProtocolo,
        @JsonProperty("sentimiento_cliente")    String sentimientoCliente,
        String resumen
) {}
