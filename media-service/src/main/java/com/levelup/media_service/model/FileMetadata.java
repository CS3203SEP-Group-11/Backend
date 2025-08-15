package com.levelup.media_service.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "files")
@Builder
public class FileMetadata {
    @Id
    private String id;
    private String filename;
    private String key;
    private FileType fileType;
    private long fileSize;
    private String ownerId;
    private Instant uploadTime;

    public enum FileType {
        IMAGE, VIDEO, PDF, DOCX
    }
}
