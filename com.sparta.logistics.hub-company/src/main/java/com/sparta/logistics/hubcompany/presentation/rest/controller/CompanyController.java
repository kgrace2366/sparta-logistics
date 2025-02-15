package com.sparta.logistics.hubcompany.presentation.rest.controller;

import com.sparta.logistics.hubcompany.application.dto.CompanyRequestDto;
import com.sparta.logistics.hubcompany.application.dto.CompanyResponseDto;
import com.sparta.logistics.hubcompany.application.dto.HubCompanyResponseDto;
import com.sparta.logistics.hubcompany.application.dto.HubResponseDto;
import com.sparta.logistics.hubcompany.application.service.CompanyService;
import com.sparta.logistics.hubcompany.domain.Company;
import com.sparta.logistics.hubcompany.domain.Hub;
import com.sparta.logistics.hubcompany.infrastructure.persistence.adaptor.CompanyQueryAdaptor;
import com.sparta.logistics.hubcompany.infrastructure.persistence.entity.CompanyEntity;
import com.sparta.logistics.hubcompany.infrastructure.persistence.search.CompanySearchCondition;
import com.sparta.logistics.hubcompany.infrastructure.persistence.search.HubSearchCondition;
import com.sparta.logistics.hubcompany.presentation.rest.dto.security.Actor;
import com.sparta.logistics.hubcompany.presentation.util.actor.LoginActor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company")
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyQueryAdaptor companyQueryAdaptor;

    @PostMapping
    public ResponseEntity<String> createCompany(@RequestBody CompanyRequestDto request,
                                                @LoginActor Actor actor) {
        CompanyResponseDto companyResponse = companyService.createCompany(request);
        Long companyId = companyResponse.getCompanyId();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Company created with ID: " + companyId);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyEntity> getCompany(@PathVariable Long companyId) {
        CompanyEntity company = companyService.getCompanyById(companyId);
        return ResponseEntity.ok(company);
    }

    @GetMapping("/{companyId}/hub")
    public ResponseEntity<HubResponseDto> getHubByCompanyId(@PathVariable Long companyId) {
        HubResponseDto hubResponse = companyService.getHubByCompanyId(companyId);
        return ResponseEntity.ok(hubResponse);
    }

    @PatchMapping("/{companyId}")
    public ResponseEntity<String> updateCompany(@PathVariable Long companyId,
                                                @RequestBody CompanyRequestDto request,
                                                @LoginActor Actor actor) {

        companyService.updateCompany(companyId, request);
        return ResponseEntity.ok("Company updated successfully.");
    }

    @GetMapping("/users/{user_id}")
    public ResponseEntity<List<HubCompanyResponseDto>> getHubsAndCompaniesByUserId(@PathVariable("user_id") Long userId) {
        List<HubCompanyResponseDto> response = companyService.getHubsAndCompaniesByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Company>> search(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "created_at") String sort,
            @RequestParam(value = "filter", required = false) String keyword
    ) {
        CompanySearchCondition searchCondition = CompanySearchCondition.of(
                String.valueOf(page),
                String.valueOf(size),
                sort,
                keyword
        );

        Page<Company> result = companyQueryAdaptor.search(searchCondition);

        return ResponseEntity.ok(result);
    }

}
