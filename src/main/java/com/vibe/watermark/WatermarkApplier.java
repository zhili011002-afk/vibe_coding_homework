package com.vibe.watermark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for applying watermarks to images.
 */
public class WatermarkApplier {
    private static final Logger logger = LoggerFactory.getLogger(WatermarkApplier.class);

    /**
     * Applies a text watermark to an image.
     * 
     * @param inputImage The input image
     * @param watermarkText The text to use as watermark
     * @param config The watermark configuration
     * @return The watermarked image
     */
    public static BufferedImage applyWatermark(BufferedImage inputImage, String watermarkText, WatermarkConfig config) {
        if (inputImage == null || watermarkText == null || watermarkText.trim().isEmpty()) {
            logger.warn("Invalid input for watermark application");
            return inputImage;
        }

        // Create a copy of the input image
        BufferedImage watermarkedImage = new BufferedImage(
            inputImage.getWidth(), 
            inputImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = watermarkedImage.createGraphics();
        
        try {
            // Draw the original image
            g2d.drawImage(inputImage, 0, 0, null);

            // Set up rendering hints for better text quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Set font and color
            Font font = config.createFont();
            g2d.setFont(font);
            g2d.setColor(config.createColorWithOpacity());

            // Calculate text position
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(watermarkText);
            int textHeight = fontMetrics.getHeight();
            
            Point position = calculateWatermarkPosition(
                watermarkedImage.getWidth(), 
                watermarkedImage.getHeight(),
                textWidth, 
                textHeight, 
                config.getPosition(), 
                config.getMargin()
            );

            // Draw the watermark text
            g2d.drawString(watermarkText, position.x, position.y);

            logger.debug("Applied watermark '{}' at position ({}, {}) to image", 
                watermarkText, position.x, position.y);

        } finally {
            g2d.dispose();
        }

        return watermarkedImage;
    }

    /**
     * Calculates the position for the watermark text based on the specified position.
     * 
     * @param imageWidth Width of the image
     * @param imageHeight Height of the image
     * @param textWidth Width of the text
     * @param textHeight Height of the text
     * @param position Desired position
     * @param margin Margin from edges
     * @return Point representing the text position
     */
    private static Point calculateWatermarkPosition(int imageWidth, int imageHeight, 
                                                   int textWidth, int textHeight, 
                                                   WatermarkPosition position, int margin) {
        int x, y;

        switch (position) {
            case TOP_LEFT:
                x = margin;
                y = margin + textHeight;
                break;
            case TOP_RIGHT:
                x = imageWidth - textWidth - margin;
                y = margin + textHeight;
                break;
            case BOTTOM_LEFT:
                x = margin;
                y = imageHeight - margin;
                break;
            case BOTTOM_RIGHT:
                x = imageWidth - textWidth - margin;
                y = imageHeight - margin;
                break;
            case CENTER:
                x = (imageWidth - textWidth) / 2;
                y = (imageHeight + textHeight) / 2;
                break;
            default:
                // Default to bottom right
                x = imageWidth - textWidth - margin;
                y = imageHeight - margin;
                break;
        }

        // Ensure the text stays within image bounds
        x = Math.max(0, Math.min(x, imageWidth - textWidth));
        y = Math.max(textHeight, Math.min(y, imageHeight));

        return new Point(x, y);
    }

    /**
     * Loads an image from a file.
     * 
     * @param imageFile The image file to load
     * @return BufferedImage or null if loading fails
     */
    public static BufferedImage loadImage(File imageFile) {
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            logger.warn("Invalid image file: {}", imageFile);
            return null;
        }

        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                logger.warn("Failed to read image: {}", imageFile.getName());
                return null;
            }
            
            logger.debug("Loaded image: {} ({}x{})", 
                imageFile.getName(), image.getWidth(), image.getHeight());
            return image;
        } catch (IOException e) {
            logger.error("Error loading image: {}", imageFile.getName(), e);
            return null;
        }
    }

    /**
     * Saves an image to a file.
     * 
     * @param image The image to save
     * @param outputFile The output file
     * @param format The image format (e.g., "jpg", "png")
     * @return true if successful, false otherwise
     */
    public static boolean saveImage(BufferedImage image, File outputFile, String format) {
        if (image == null || outputFile == null || format == null) {
            logger.warn("Invalid parameters for saving image");
            return false;
        }

        try {
            // Create parent directories if they don't exist
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    logger.error("Failed to create parent directories for: {}", outputFile.getPath());
                    return false;
                }
            }

            boolean success = ImageIO.write(image, format, outputFile);
            if (success) {
                logger.debug("Saved watermarked image: {}", outputFile.getName());
            } else {
                logger.error("Failed to save image: {}", outputFile.getName());
            }
            return success;
        } catch (IOException e) {
            logger.error("Error saving image: {}", outputFile.getName(), e);
            return false;
        }
    }

    /**
     * Gets the file extension of an image file.
     * 
     * @param file The image file
     * @return The file extension (without dot) or "jpg" as default
     */
    public static String getImageFormat(File file) {
        if (file == null) {
            return "jpg";
        }

        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".png")) {
            return "png";
        } else if (fileName.endsWith(".gif")) {
            return "gif";
        } else if (fileName.endsWith(".bmp")) {
            return "bmp";
        } else if (fileName.endsWith(".tiff") || fileName.endsWith(".tif")) {
            return "tiff";
        } else {
            // Default to JPEG for jpg, jpeg, and unknown formats
            return "jpg";
        }
    }

    /**
     * Creates the output filename for a watermarked image.
     * 
     * @param originalFile The original image file
     * @param outputDir The output directory
     * @return The output file
     */
    public static File createOutputFile(File originalFile, File outputDir) {
        if (originalFile == null || outputDir == null) {
            return null;
        }

        String originalName = originalFile.getName();
        String nameWithoutExt;
        String extension;

        int lastDotIndex = originalName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            nameWithoutExt = originalName.substring(0, lastDotIndex);
            extension = originalName.substring(lastDotIndex);
        } else {
            nameWithoutExt = originalName;
            extension = ".jpg";
        }

        String outputFileName = nameWithoutExt + "_watermarked" + extension;
        return new File(outputDir, outputFileName);
    }
}