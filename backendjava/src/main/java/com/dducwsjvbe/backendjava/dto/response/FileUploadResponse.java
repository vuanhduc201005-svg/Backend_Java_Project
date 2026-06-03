package com.dducwsjvbe.backendjava.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse implements Serializable {
    private String fileName;
    private Long fileSize;
    private Date uploadedAt;
    private double uploadProgress;
    private boolean completed;
    private int chunkIndex;
    private int totalChunks;
}
