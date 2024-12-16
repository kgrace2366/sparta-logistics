package com.sparta.logistics.delivery.presentation.controller;

import com.sparta.logistics.delivery.application.dto.DeliveryCreateRequestDto;
import com.sparta.logistics.delivery.application.dto.DeliveryUpdateRequestDto;
import com.sparta.logistics.delivery.application.service.DeliveryService;
import com.sparta.logistics.delivery.domain.Delivery;
import com.sparta.logistics.delivery.infrastructure.persistence.search.DeliverySearchCondition;
import com.sparta.logistics.delivery.infrastructure.persistence.security.Actor;
import com.sparta.logistics.delivery.presentation.dto.DeliveryResponseDto;
import com.sparta.logistics.delivery.presentation.util.actor.LoginActor;
import com.sparta.logistics.delivery.presentation.util.search.SearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery")
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PostMapping()
    public ResponseEntity<?> createDelivery(@RequestBody DeliveryCreateRequestDto requestDto) {
        DeliveryResponseDto responseDto = DeliveryResponseDto.from(deliveryService.createDelivery(requestDto));

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<?> getDelivery(@PathVariable Long deliveryId) {
        DeliveryResponseDto delivery = deliveryService.getDelivery(deliveryId);

        return ResponseEntity.ok(delivery);
    }

    @GetMapping()
    public ResponseEntity<?> getDeliveryList(@SearchCondition DeliverySearchCondition deliverySearchCondition) {
        Page<DeliveryResponseDto> deliveryList = deliveryService.getDeliveryList(deliverySearchCondition);

        return ResponseEntity.ok(deliveryList);
    }

    @PatchMapping("/{deliveryId}")
    public ResponseEntity<?> updateDelivery(@PathVariable Long deliveryId,
                                            @RequestBody DeliveryUpdateRequestDto updateRequestDto,
                                            @LoginActor Actor actor
    ) {
        Delivery domain = updateRequestDto.toDomain();
        Delivery delivery = deliveryService.updateDelivery(domain, actor.userId());

        return ResponseEntity.ok(DeliveryResponseDto.from(delivery));
    }
}