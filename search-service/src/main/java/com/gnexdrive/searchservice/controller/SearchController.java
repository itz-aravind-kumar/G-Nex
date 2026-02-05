package com.gnexdrive.searchservice.controller;

import com.gnexdrive.common.dto.ApiResponse;
import com.gnexdrive.searchservice.dto.SearchRequest;
import com.gnexdrive.searchservice.dto.SearchResponse;
import com.gnexdrive.searchservice.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for search operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "File search operations")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Search files by query")
    @GetMapping
    public ResponseEntity<ApiResponse<SearchResponse>> searchFiles(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // TODO: Implement file search endpoint
        return null;
    }

    @Operation(summary = "Advanced search with filters")
    @PostMapping("/advanced")
    public ResponseEntity<ApiResponse<SearchResponse>> advancedSearch(
            @RequestBody SearchRequest searchRequest) {
        // TODO: Implement advanced search endpoint
        return null;
    }

    @Operation(summary = "Get search suggestions")
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<Object>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(required = false) String userId) {
        // TODO: Implement search suggestions endpoint
        return null;
    }
}
