package com.levelup.media_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "files")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false, unique = true)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Column(nullable = false)
    private long fileSize;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "upload_time", nullable = false)
    private Instant uploadTime;

    public enum FileType {
        IMAGE, VIDEO, PDF, DOCX
    }
}
