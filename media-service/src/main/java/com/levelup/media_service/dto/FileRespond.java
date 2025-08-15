package com.levelup.media_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileRespond {
    private String id;
    private String fileUrl;
}
