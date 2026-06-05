package com.callkeypoints.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeepSeekExtractedData(
        String cliente,
        String direccion,
        String problematica,
        @JsonProperty("persona_contacto") String personaContacto,
        @JsonProperty("solucion_sugerida") String solucionSugerida,
        String resumen
) {}
