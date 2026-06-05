package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.dto.CallDetailResponse;
import com.callkeypoints.backend.model.dto.CallRequest;
import com.callkeypoints.backend.model.dto.CallSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface CallService {
    CallDetailResponse createCall(CallRequest request, UUID userId);
    List<CallSummaryResponse> listCalls(UUID userId);
    CallDetailResponse getCall(Long id, UUID userId);
    void deleteCall(Long id, UUID userId);
}
