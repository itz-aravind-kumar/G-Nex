package com.gnexdrive.searchservice.service;

import com.gnexdrive.searchservice.document.FileDocument;
import com.gnexdrive.searchservice.dto.SearchRequest;
import com.gnexdrive.searchservice.dto.SearchResponse;

import java.util.List;

/**
 * Service interface for search operations
 */
public interface SearchService {

    /**
     * Index a file document in Elasticsearch
     */
    void indexFile(FileDocument fileDocument);

    /**
     * Search files by query
     */
    SearchResponse searchFiles(String query, String type, String userId, int page, int size);

    /**
     * Advanced search with filters
     */
    SearchResponse advancedSearch(SearchRequest searchRequest);

    /**
     * Get search suggestions
     */
    List<String> getSearchSuggestions(String query, String userId);

    /**
     * Delete file from index
     */
    void deleteFileFromIndex(String fileId);

    /**
     * Update file in index
     */
    void updateFileInIndex(FileDocument fileDocument);
}
