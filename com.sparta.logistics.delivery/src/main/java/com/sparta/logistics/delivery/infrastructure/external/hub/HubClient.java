package com.sparta.logistics.delivery.infrastructure.external.hub;

import com.sparta.logistics.delivery.infrastructure.external.hub.dto.HubResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hub-service")
public interface HubClient {
    @GetMapping("/api/v1/hubs/{hubId}")
    ResponseEntity<HubResponseDto> getHubById(@PathVariable Long hubId);
}