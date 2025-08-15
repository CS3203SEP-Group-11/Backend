package com.levelup.media_service.repository;

import com.levelup.media_service.model.FileMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FileRepository extends MongoRepository<FileMetadata, String> {
    Optional<FileMetadata> findByKey(String key);

}
