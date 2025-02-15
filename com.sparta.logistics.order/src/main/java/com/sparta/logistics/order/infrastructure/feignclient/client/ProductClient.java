package com.sparta.logistics.order.infrastructure.feignclient.client;

import com.sparta.logistics.order.infrastructure.feignclient.dto.ProductClientQuantityRequest;
import com.sparta.logistics.order.infrastructure.feignclient.dto.ProductClientQuantityResponse;
import com.sparta.logistics.order.infrastructure.feignclient.dto.ProductClientResponse;
import com.sparta.logistics.order.presentation.rest.util.ApiResponse.Success;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/v1/products/{productId}")
    ResponseEntity<Success<ProductClientResponse>> getProduct(@PathVariable("productId") Long productId);

    @PostMapping("/api/v1/products/{productId}/quantity")
    ResponseEntity<Success<ProductClientQuantityResponse>> changeProductQuantity(
        @PathVariable("productId") Long productId,
        @RequestBody ProductClientQuantityRequest request
    );
}
