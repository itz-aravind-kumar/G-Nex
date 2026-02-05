package com.gnexdrive.searchservice.dto;

import com.gnexdrive.common.dto.FileMetadataDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Search response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    private List<FileMetadataDto> results;
    private long totalResults;
    private int currentPage;
    private int totalPages;
    private long searchTimeMs;
    private String[] suggestions;
}
