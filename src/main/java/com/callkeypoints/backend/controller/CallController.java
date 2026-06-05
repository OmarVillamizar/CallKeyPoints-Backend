package com.callkeypoints.backend.controller;

import com.callkeypoints.backend.model.dto.CallDetailResponse;
import com.callkeypoints.backend.model.dto.CallRequest;
import com.callkeypoints.backend.model.dto.CallSummaryResponse;
import com.callkeypoints.backend.service.CallService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/calls")
@Validated
public class CallController {

    private final CallService callService;

    public CallController(CallService callService) {
        this.callService = callService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CallDetailResponse createCall(@Valid @RequestBody CallRequest request, Authentication auth) {
        return callService.createCall(request, userId(auth));
    }

    @GetMapping
    public List<CallSummaryResponse> listCalls(Authentication auth) {
        return callService.listCalls(userId(auth));
    }

    @GetMapping("/{id}")
    public CallDetailResponse getCall(@PathVariable Long id, Authentication auth) {
        return callService.getCall(id, userId(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCall(@PathVariable Long id, Authentication auth) {
        callService.deleteCall(id, userId(auth));
    }

    private UUID userId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
