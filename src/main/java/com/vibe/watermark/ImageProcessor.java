package com.vibe.watermark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main processor class that handles image processing operations.
 */
public class ImageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessor.class);
    
    private final WatermarkConfig config;
    private int processedCount = 0;
    private int skippedCount = 0;
    private int errorCount = 0;

    public ImageProcessor(WatermarkConfig config) {
        this.config = config;
    }

    /**
     * Processes a single image file.
     * 
     * @param imageFile The image file to process
     * @return true if successful, false otherwise
     */
    public boolean processSingleFile(File imageFile) {
        if (!ExifReader.isSupportedImageFile(imageFile)) {
            logger.warn("Unsupported file format: {}", imageFile.getName());
            skippedCount++;
            return false;
        }

        // Create output directory
        File parentDir = imageFile.getParentFile();
        File outputDir = new File(parentDir, parentDir.getName() + "_watermark");
        
        return processImageFile(imageFile, outputDir);
    }

    /**
     * Processes all image files in a directory.
     * 
     * @param inputDir The input directory containing images
     * @return true if at least one file was processed successfully
     */
    public boolean processDirectory(File inputDir) {
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            logger.error("Invalid input directory: {}", inputDir.getPath());
            return false;
        }

        // Create output directory
        File outputDir = new File(inputDir, inputDir.getName() + "_watermark");
        
        // Find all image files
        List<File> imageFiles = findImageFiles(inputDir);
        
        if (imageFiles.isEmpty()) {
            System.out.println("No supported image files found in directory: " + inputDir.getPath());
            return false;
        }

        System.out.println("Found " + imageFiles.size() + " image files to process");
        System.out.println("Output directory: " + outputDir.getPath());

        boolean anySuccess = false;
        
        // Process each image file
        for (File imageFile : imageFiles) {
            boolean success = processImageFile(imageFile, outputDir);
            if (success) {
                anySuccess = true;
            }
        }

        // Print summary
        printProcessingSummary();
        
        return anySuccess;
    }

    /**
     * Processes a single image file and saves it to the output directory.
     * 
     * @param imageFile The image file to process
     * @param outputDir The output directory
     * @return true if successful, false otherwise
     */
    private boolean processImageFile(File imageFile, File outputDir) {
        try {
            System.out.println("Processing: " + imageFile.getName());

            // Load the image
            BufferedImage image = WatermarkApplier.loadImage(imageFile);
            if (image == null) {
                logger.error("Failed to load image: {}", imageFile.getName());
                errorCount++;
                return false;
            }

            // Extract date from EXIF or use file date as fallback
            String dateText = ExifReader.extractDateWithFallback(imageFile);
            if (dateText == null || dateText.trim().isEmpty()) {
                logger.warn("No date information found for: {}", imageFile.getName());
                dateText = "Unknown Date";
            }

            // Apply watermark
            BufferedImage watermarkedImage = WatermarkApplier.applyWatermark(image, dateText, config);
            if (watermarkedImage == null) {
                logger.error("Failed to apply watermark to: {}", imageFile.getName());
                errorCount++;
                return false;
            }

            // Create output file
            File outputFile = WatermarkApplier.createOutputFile(imageFile, outputDir);
            if (outputFile == null) {
                logger.error("Failed to create output file path for: {}", imageFile.getName());
                errorCount++;
                return false;
            }

            // Save the watermarked image
            String format = WatermarkApplier.getImageFormat(imageFile);
            boolean saved = WatermarkApplier.saveImage(watermarkedImage, outputFile, format);
            
            if (saved) {
                System.out.println("  ✓ Saved: " + outputFile.getName() + " (watermark: " + dateText + ")");
                processedCount++;
                return true;
            } else {
                logger.error("Failed to save watermarked image: {}", outputFile.getName());
                errorCount++;
                return false;
            }

        } catch (Exception e) {
            logger.error("Unexpected error processing file: {}", imageFile.getName(), e);
            errorCount++;
            return false;
        }
    }

    /**
     * Recursively finds all image files in a directory.
     * 
     * @param directory The directory to search
     * @return List of image files
     */
    private List<File> findImageFiles(File directory) {
        List<File> imageFiles = new ArrayList<>();
        
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return imageFiles;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return imageFiles;
        }

        for (File file : files) {
            if (file.isFile() && ExifReader.isSupportedImageFile(file)) {
                imageFiles.add(file);
            } else if (file.isDirectory()) {
                // Recursively search subdirectories (optional)
                // Comment out the next line if you don't want recursive search
                // imageFiles.addAll(findImageFiles(file));
            }
        }

        return imageFiles;
    }

    /**
     * Prints a summary of the processing results.
     */
    private void printProcessingSummary() {
        System.out.println("\n" + "====================================================");
        System.out.println("Processing Summary:");
        System.out.println("  Files processed successfully: " + processedCount);
        System.out.println("  Files skipped: " + skippedCount);
        System.out.println("  Files with errors: " + errorCount);
        System.out.println("  Total files: " + (processedCount + skippedCount + errorCount));
        System.out.println("====================================================");
    }

    /**
     * Validates the watermark configuration.
     * 
     * @param config The configuration to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateConfig(WatermarkConfig config) {
        if (config == null) {
            logger.error("Watermark configuration is null");
            return false;
        }

        if (config.getFontSize() <= 0) {
            logger.error("Invalid font size: {}", config.getFontSize());
            return false;
        }

        if (config.getOpacity() < 0.0f || config.getOpacity() > 1.0f) {
            logger.error("Invalid opacity: {}", config.getOpacity());
            return false;
        }

        if (config.getMargin() < 0) {
            logger.error("Invalid margin: {}", config.getMargin());
            return false;
        }

        return true;
    }

    // Getters for processing statistics
    public int getProcessedCount() {
        return processedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void resetCounters() {
        processedCount = 0;
        skippedCount = 0;
        errorCount = 0;
    }
}