package com.gnexdrive.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Utility class for file operations
 */
@Slf4j
@UtilityClass
public class FileUtils {
    
    /**
     * Generate unique file ID using UUID
     */
    public static String generateFileId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Extract file extension from filename
     * @param fileName the file name
     * @return file extension (e.g., "pdf", "jpg") or empty string if no extension
     */
    public static String extractFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * Calculate MD5 checksum for file content
     * @param content file content as byte array
     * @return MD5 checksum as hexadecimal string
     */
    public static String calculateChecksum(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not found", e);
            return "";
        }
    }
    
    /**
     * Sanitize filename by removing special characters and spaces
     * @param fileName original filename
     * @return sanitized filename
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "file";
        }
        
        // Remove path separators and special characters
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        
        // Remove multiple consecutive underscores
        sanitized = sanitized.replaceAll("_{2,}", "_");
        
        // Remove leading/trailing underscores
        sanitized = sanitized.replaceAll("^_+|_+$", "");
        
        // If sanitization resulted in empty string, use default
        return sanitized.isEmpty() ? "file" : sanitized;
    }
    
    /**
     * Validate if file type is in allowed types list
     * @param fileType MIME type of the file
     * @param allowedTypes array of allowed MIME types
     * @return true if file type is allowed
     */
    public static boolean isValidFileType(String fileType, String[] allowedTypes) {
        if (fileType == null || fileType.isEmpty()) {
            return false;
        }
        
        if (allowedTypes == null || allowedTypes.length == 0) {
            return true; // If no restrictions, allow all
        }
        
        return Arrays.stream(allowedTypes)
                .anyMatch(allowed -> fileType.equalsIgnoreCase(allowed) || 
                                   fileType.startsWith(allowed.split("/")[0] + "/"));
    }
    
    /**
     * Format file size in human-readable format (KB, MB, GB)
     * @param sizeInBytes file size in bytes
     * @return formatted file size string
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 0) {
            return "0 B";
        }
        
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        }
        
        double size = sizeInBytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unitIndex = -1;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
