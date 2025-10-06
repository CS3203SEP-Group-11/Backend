package com.levelup.media_service.repository;

import com.levelup.media_service.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileMetadata, UUID> {
    Optional<FileMetadata> findByKey(String key);
}
