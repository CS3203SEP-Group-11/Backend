package com.levelup.auth_service.repository;

import com.levelup.auth_service.model.AuthUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AuthRepository extends MongoRepository<AuthUser, String> {
    Optional<AuthUser> findByEmail(String email);
    Optional<AuthUser> findByGoogleId(String googleId);
    boolean existsByEmail(String email);
}
