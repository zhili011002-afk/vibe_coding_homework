package com.vibe.watermark;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Utility class for reading EXIF data from image files.
 */
public class ExifReader {
    private static final Logger logger = LoggerFactory.getLogger(ExifReader.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Extracts the date taken from the EXIF data of an image file.
     * 
     * @param imageFile The image file to read EXIF data from
     * @return The formatted date string (yyyy-MM-dd) or null if not found
     */
    public static String extractDateTaken(File imageFile) {
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            logger.warn("Invalid image file: {}", imageFile);
            return null;
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            
            // Try to get date from EXIF SubIFD directory (most common location)
            ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifSubIFDDirectory != null) {
                Date dateTaken = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (dateTaken != null) {
                    return formatDate(dateTaken);
                }
                
                // Try alternative date tag
                dateTaken = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
                if (dateTaken != null) {
                    return formatDate(dateTaken);
                }
            }

            // If no date found in EXIF SubIFD, try other directories
            for (Directory directory : metadata.getDirectories()) {
                Date dateTaken = null;
                
                // Try different date tags
                if (directory.hasTagName(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                    dateTaken = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                } else if (directory.hasTagName(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED)) {
                    dateTaken = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
                } else if (directory.hasTagName(ExifSubIFDDirectory.TAG_DATETIME)) {
                    dateTaken = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME);
                }
                
                if (dateTaken != null) {
                    return formatDate(dateTaken);
                }
            }

            logger.info("No date information found in EXIF data for file: {}", imageFile.getName());
            return null;

        } catch (ImageProcessingException e) {
            logger.error("Error processing image metadata for file: {}", imageFile.getName(), e);
            return null;
        } catch (IOException e) {
            logger.error("IO error reading file: {}", imageFile.getName(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error reading EXIF data for file: {}", imageFile.getName(), e);
            return null;
        }
    }

    /**
     * Formats a Date object to yyyy-MM-dd string format.
     * 
     * @param date The date to format
     * @return Formatted date string
     */
    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        }

        try {
            // Convert Date to LocalDate and format
            LocalDate localDate = date.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            return localDate.format(DATE_FORMATTER);
        } catch (Exception e) {
            logger.error("Error formatting date: {}", date, e);
            return null;
        }
    }

    /**
     * Checks if a file is a supported image format based on its extension.
     * 
     * @param file The file to check
     * @return true if the file is a supported image format
     */
    public static boolean isSupportedImageFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || 
               fileName.endsWith(".tiff") || 
               fileName.endsWith(".tif") ||
               fileName.endsWith(".bmp") ||
               fileName.endsWith(".gif");
    }

    /**
     * Gets the file's last modified date as a fallback when EXIF date is not available.
     * 
     * @param file The file to get the date from
     * @return Formatted date string (yyyy-MM-dd) or null if error
     */
    public static String getFileModifiedDate(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        try {
            long lastModified = file.lastModified();
            Date date = new Date(lastModified);
            return formatDate(date);
        } catch (Exception e) {
            logger.error("Error getting file modified date for: {}", file.getName(), e);
            return null;
        }
    }

    /**
     * Extracts date from image file with fallback to file modification date.
     * 
     * @param imageFile The image file
     * @return Date string in yyyy-MM-dd format, or "Unknown Date" if neither EXIF nor file date available
     */
    public static String extractDateWithFallback(File imageFile) {
        // First try to get date from EXIF
        String exifDate = extractDateTaken(imageFile);
        if (exifDate != null) {
            logger.debug("Using EXIF date for {}: {}", imageFile.getName(), exifDate);
            return exifDate;
        }

        // Fallback to file modification date
        String fileDate = getFileModifiedDate(imageFile);
        if (fileDate != null) {
            logger.debug("Using file modification date for {}: {}", imageFile.getName(), fileDate);
            return fileDate;
        }

        // Last resort
        logger.warn("No date available for file: {}", imageFile.getName());
        return "Unknown Date";
    }
}