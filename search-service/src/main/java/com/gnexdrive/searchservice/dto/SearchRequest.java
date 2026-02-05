package com.gnexdrive.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Search request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    private String query;
    private String userId;
    private String fileType;
    private Long minSize;
    private Long maxSize;
    private LocalDateTime uploadedAfter;
    private LocalDateTime uploadedBefore;
    private String[] tags;
    private int page;
    private int size;
    private String sortBy;
    private String sortOrder;
}
