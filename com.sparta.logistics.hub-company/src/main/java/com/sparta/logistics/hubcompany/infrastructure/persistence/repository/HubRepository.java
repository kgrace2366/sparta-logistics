package com.sparta.logistics.hubcompany.infrastructure.persistence.repository;

import com.sparta.logistics.hubcompany.infrastructure.persistence.entity.HubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRepository extends JpaRepository<HubEntity, Long> {
}