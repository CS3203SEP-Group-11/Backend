package com.levelup.media_service.controller;

import com.levelup.media_service.dto.FileRespond;
import com.levelup.media_service.model.FileMetadata;
import com.levelup.media_service.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final FileStorageService storageService;

    @GetMapping("/health")
    public String healthCheck() {
        return "File Service is up and running!";
    }

    @PostMapping("/upload")
    public ResponseEntity<FileRespond> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") FileMetadata.FileType fileType,
            @RequestHeader("X-User-Id") String userId
    ) throws IOException {
        log.info("Received file upload request: {} of type {}", file.getOriginalFilename(), fileType);
        return ResponseEntity.ok(storageService.uploadFile(file, fileType, userId));
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<String> deleteFile(
            @PathVariable String fileId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(storageService.deleteFile(fileId, userId));
    }


    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        byte[] fileData = storageService.downloadFile(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(fileData);
    }

}
