package com.levelup.media_service.service;

import com.levelup.media_service.dto.FileRespond;
import com.levelup.media_service.exception.FileNotFoundException;
import com.levelup.media_service.exception.UnauthorizedActionException;
import com.levelup.media_service.model.FileMetadata;
import com.levelup.media_service.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final S3Client s3Client;
    private final FileRepository fileRepository;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    @Transactional
    public FileRespond uploadFile(MultipartFile file, FileMetadata.FileType fileType, String userId) throws IOException {

        String folder = switch (fileType) {
            case IMAGE -> "images/";
            case VIDEO -> "videos/";
            case PDF -> "pdfs/";
            case DOCX -> "docs/";
            default -> "others/";
        };

        String key = folder + UUID.randomUUID().toString();

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentDisposition("inline")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            FileMetadata fileMetadata = getFileMetadata(key, file.getOriginalFilename(), fileType, file.getSize(), userId);

            fileRepository.save(fileMetadata);

            return createFileResponse(fileMetadata);

        } catch (Exception e) {
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build());
            } catch (Exception ex) {
                throw new RuntimeException("Failed to delete file after upload failure: " + ex.getMessage(), ex);
            }
            throw new FileUploadException("Failed to upload file");
        }
    }

    @Transactional
    public String deleteFile(String fileId, String userId) {
        FileMetadata fileMetadata = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found"));

        if (!fileMetadata.getOwnerId().equals(userId)) {
            throw new UnauthorizedActionException("User does not have permission to delete this file");
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileMetadata.getKey())
                .build());

        fileRepository.delete(fileMetadata);

        return "File deleted successfully";
    }

    public byte[] downloadFile(String fileName) {
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build());
        return objectAsBytes.asByteArray();
    }

    private String generateFileUrl(String key) {
        GetUrlRequest request = GetUrlRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Client.utilities().getUrl(request).toExternalForm();
    }

    private FileMetadata getFileMetadata(String key, String filename, FileMetadata.FileType fileType, long fileSize, String ownerId) {
        return FileMetadata.builder()
                .key(key)
                .filename(filename)
                .fileType(fileType)
                .fileSize(fileSize)
                .ownerId(ownerId)
                .uploadTime(Instant.now())
                .build();
    }

    private FileRespond createFileResponse(FileMetadata fileMetadata) {
        return FileRespond.builder()
                .id(fileMetadata.getId())
                .fileUrl(generateFileUrl(fileMetadata.getKey()))
                .build();
    }
}