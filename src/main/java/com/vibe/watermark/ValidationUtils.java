package com.vibe.watermark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

/**
 * Utility class for input validation and error handling.
 */
public class ValidationUtils {
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);

    private static final String[] SUPPORTED_EXTENSIONS = {
        ".jpg", ".jpeg", ".png", ".tiff", ".tif", ".bmp", ".gif"
    };

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB

    /**
     * Validates if the given path is a valid file system path.
     * 
     * @param path The path to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        try {
            Paths.get(path);
            return true;
        } catch (InvalidPathException e) {
            logger.error("Invalid path format: {}", path, e);
            return false;
        }
    }

    /**
     * Validates if the file exists and is accessible.
     * 
     * @param file The file to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidFile(File file) {
        if (file == null) {
            logger.error("File is null");
            return false;
        }

        if (!file.exists()) {
            logger.error("File does not exist: {}", file.getPath());
            return false;
        }

        if (!file.canRead()) {
            logger.error("File is not readable: {}", file.getPath());
            return false;
        }

        return true;
    }

    /**
     * Validates if the directory exists and is accessible.
     * 
     * @param directory The directory to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDirectory(File directory) {
        if (directory == null) {
            logger.error("Directory is null");
            return false;
        }

        if (!directory.exists()) {
            logger.error("Directory does not exist: {}", directory.getPath());
            return false;
        }

        if (!directory.isDirectory()) {
            logger.error("Path is not a directory: {}", directory.getPath());
            return false;
        }

        if (!directory.canRead()) {
            logger.error("Directory is not readable: {}", directory.getPath());
            return false;
        }

        return true;
    }

    /**
     * Validates if the file is a supported image format.
     * 
     * @param file The file to validate
     * @return true if supported, false otherwise
     */
    public static boolean isSupportedImageFormat(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        for (String extension : SUPPORTED_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }

        logger.debug("Unsupported image format: {}", fileName);
        return false;
    }

    /**
     * Validates if the file size is within acceptable limits.
     * 
     * @param file The file to validate
     * @return true if size is acceptable, false otherwise
     */
    public static boolean isFileSizeAcceptable(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        long fileSize = file.length();
        if (fileSize > MAX_FILE_SIZE) {
            logger.warn("File size too large: {} bytes (max: {} bytes)", fileSize, MAX_FILE_SIZE);
            return false;
        }

        if (fileSize == 0) {
            logger.warn("File is empty: {}", file.getName());
            return false;
        }

        return true;
    }

    /**
     * Validates watermark configuration parameters.
     * 
     * @param config The configuration to validate
     * @return ValidationResult containing validation status and error messages
     */
    public static ValidationResult validateWatermarkConfig(WatermarkConfig config) {
        ValidationResult result = new ValidationResult();

        if (config == null) {
            result.addError("Watermark configuration is null");
            return result;
        }

        // Validate font size
        if (config.getFontSize() <= 0) {
            result.addError("Font size must be positive, got: " + config.getFontSize());
        } else if (config.getFontSize() > 200) {
            result.addWarning("Font size is very large: " + config.getFontSize());
        }

        // Validate opacity
        if (config.getOpacity() < 0.0f || config.getOpacity() > 1.0f) {
            result.addError("Opacity must be between 0.0 and 1.0, got: " + config.getOpacity());
        }

        // Validate margin
        if (config.getMargin() < 0) {
            result.addError("Margin must be non-negative, got: " + config.getMargin());
        } else if (config.getMargin() > 500) {
            result.addWarning("Margin is very large: " + config.getMargin());
        }

        // Validate color
        if (config.getColor() == null) {
            result.addError("Color cannot be null");
        }

        // Validate position
        if (config.getPosition() == null) {
            result.addError("Position cannot be null");
        }

        return result;
    }

    /**
     * Validates if the output directory can be created or is writable.
     * 
     * @param outputDir The output directory
     * @return true if valid, false otherwise
     */
    public static boolean isValidOutputDirectory(File outputDir) {
        if (outputDir == null) {
            logger.error("Output directory is null");
            return false;
        }

        // If directory exists, check if it's writable
        if (outputDir.exists()) {
            if (!outputDir.isDirectory()) {
                logger.error("Output path exists but is not a directory: {}", outputDir.getPath());
                return false;
            }
            if (!outputDir.canWrite()) {
                logger.error("Output directory is not writable: {}", outputDir.getPath());
                return false;
            }
            return true;
        }

        // If directory doesn't exist, check if parent is writable
        File parent = outputDir.getParentFile();
        if (parent == null) {
            logger.error("Cannot determine parent directory for: {}", outputDir.getPath());
            return false;
        }

        if (!parent.exists()) {
            logger.error("Parent directory does not exist: {}", parent.getPath());
            return false;
        }

        if (!parent.canWrite()) {
            logger.error("Parent directory is not writable: {}", parent.getPath());
            return false;
        }

        return true;
    }

    /**
     * Gets a human-readable error message for common file system issues.
     * 
     * @param file The problematic file
     * @param operation The operation being attempted
     * @return Error message
     */
    public static String getFileErrorMessage(File file, String operation) {
        if (file == null) {
            return "File is null for operation: " + operation;
        }

        if (!file.exists()) {
            return String.format("File not found for %s: %s", operation, file.getPath());
        }

        if (file.isDirectory() && operation.contains("read file")) {
            return String.format("Expected file but found directory: %s", file.getPath());
        }

        if (file.isFile() && operation.contains("read directory")) {
            return String.format("Expected directory but found file: %s", file.getPath());
        }

        if (!file.canRead()) {
            return String.format("Permission denied reading %s: %s", 
                file.isDirectory() ? "directory" : "file", file.getPath());
        }

        if (!file.canWrite() && operation.contains("write")) {
            return String.format("Permission denied writing to %s: %s", 
                file.isDirectory() ? "directory" : "file", file.getPath());
        }

        return String.format("Unknown error with %s for operation: %s", file.getPath(), operation);
    }

    /**
     * Result class for validation operations.
     */
    public static class ValidationResult {
        private boolean valid = true;
        private StringBuilder errors = new StringBuilder();
        private StringBuilder warnings = new StringBuilder();

        public void addError(String error) {
            valid = false;
            if (errors.length() > 0) {
                errors.append("; ");
            }
            errors.append(error);
        }

        public void addWarning(String warning) {
            if (warnings.length() > 0) {
                warnings.append("; ");
            }
            warnings.append(warning);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrors() {
            return errors.toString();
        }

        public String getWarnings() {
            return warnings.toString();
        }

        public boolean hasWarnings() {
            return warnings.length() > 0;
        }
    }
}