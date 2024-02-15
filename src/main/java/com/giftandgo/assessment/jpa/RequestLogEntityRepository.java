package com.giftandgo.assessment.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestLogEntityRepository extends JpaRepository<RequestLogEntity, UUID> {
}
