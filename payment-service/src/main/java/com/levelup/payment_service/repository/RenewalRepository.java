package com.levelup.payment_service.repository;

import com.levelup.payment_service.model.Renewal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RenewalRepository extends JpaRepository<Renewal, UUID> {
}