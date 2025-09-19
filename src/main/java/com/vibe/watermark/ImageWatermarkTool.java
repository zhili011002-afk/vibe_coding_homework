package com.vibe.watermark;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.Arrays;

/**
 * Main application class for the Image Watermark Tool.
 * This tool adds date watermarks to images based on their EXIF data.
 */
public class ImageWatermarkTool {
    private static final Logger logger = LoggerFactory.getLogger(ImageWatermarkTool.class);

    public static void main(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                printHelp(formatter, options);
                return;
            }

            // Parse required and optional arguments
            String inputPath = cmd.getOptionValue("input");
            if (inputPath == null) {
                System.err.println("Error: Input path is required");
                printHelp(formatter, options);
                System.exit(1);
            }

            // Parse watermark configuration
            WatermarkConfig config = parseWatermarkConfig(cmd);
            
            // Validate input path
            if (!ValidationUtils.isValidPath(inputPath)) {
                System.err.println("Error: Invalid path format: " + inputPath);
                System.exit(1);
            }

            File inputFile = new File(inputPath);
            if (!ValidationUtils.isValidFile(inputFile) && !ValidationUtils.isValidDirectory(inputFile)) {
                System.err.println("Error: " + ValidationUtils.getFileErrorMessage(inputFile, "read"));
                System.exit(1);
            }

            // Validate watermark configuration
            ValidationUtils.ValidationResult validationResult = ValidationUtils.validateWatermarkConfig(config);
            if (!validationResult.isValid()) {
                System.err.println("Error: Invalid configuration - " + validationResult.getErrors());
                System.exit(1);
            }
            if (validationResult.hasWarnings()) {
                System.out.println("Warning: " + validationResult.getWarnings());
            }

            // Process images
            ImageProcessor processor = new ImageProcessor(config);
            if (inputFile.isDirectory()) {
                processor.processDirectory(inputFile);
            } else if (inputFile.isFile()) {
                processor.processSingleFile(inputFile);
            } else {
                System.err.println("Error: Invalid input path: " + inputPath);
                System.exit(1);
            }

        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            printHelp(formatter, options);
            System.exit(1);
        } catch (Exception e) {
            logger.error("Unexpected error occurred", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Creates command line options for the application.
     */
    private static Options createOptions() {
        Options options = new Options();

        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg()
                .desc("Input image file or directory path")
                .build());

        options.addOption(Option.builder("s")
                .longOpt("size")
                .hasArg()
                .desc("Font size for watermark (default: 24)")
                .build());

        options.addOption(Option.builder("c")
                .longOpt("color")
                .hasArg()
                .desc("Watermark color in hex format (default: #FFFFFF)")
                .build());

        options.addOption(Option.builder("p")
                .longOpt("position")
                .hasArg()
                .desc("Watermark position: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER (default: BOTTOM_RIGHT)")
                .build());

        options.addOption(Option.builder("o")
                .longOpt("opacity")
                .hasArg()
                .desc("Watermark opacity (0.0-1.0, default: 0.8)")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Show this help message")
                .build());

        return options;
    }

    /**
     * Parses watermark configuration from command line arguments.
     */
    private static WatermarkConfig parseWatermarkConfig(CommandLine cmd) {
        WatermarkConfig config = new WatermarkConfig();

        // Font size
        if (cmd.hasOption("size")) {
            try {
                config.setFontSize(Integer.parseInt(cmd.getOptionValue("size")));
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid font size, using default");
            }
        }

        // Color
        if (cmd.hasOption("color")) {
            try {
                String colorStr = cmd.getOptionValue("color");
                if (!colorStr.startsWith("#")) {
                    colorStr = "#" + colorStr;
                }
                config.setColor(Color.decode(colorStr));
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid color format, using default");
            }
        }

        // Position
        if (cmd.hasOption("position")) {
            try {
                String positionStr = cmd.getOptionValue("position").toUpperCase();
                config.setPosition(WatermarkPosition.valueOf(positionStr));
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Invalid position, using default. Valid positions: " + 
                    Arrays.toString(WatermarkPosition.values()));
            }
        }

        // Opacity
        if (cmd.hasOption("opacity")) {
            try {
                float opacity = Float.parseFloat(cmd.getOptionValue("opacity"));
                if (opacity >= 0.0f && opacity <= 1.0f) {
                    config.setOpacity(opacity);
                } else {
                    System.err.println("Warning: Opacity must be between 0.0 and 1.0, using default");
                }
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid opacity format, using default");
            }
        }

        return config;
    }

    /**
     * Prints help information.
     */
    private static void printHelp(HelpFormatter formatter, Options options) {
        formatter.printHelp("java -jar image-watermark.jar", 
            "Image Watermark Tool - Adds date watermarks to images based on EXIF data\n\n", 
            options, 
            "\nExamples:\n" +
            "  java -jar image-watermark.jar -i /path/to/images\n" +
            "  java -jar image-watermark.jar -i /path/to/image.jpg -s 32 -c #FF0000 -p TOP_LEFT\n" +
            "  java -jar image-watermark.jar -i /path/to/images -s 28 -c #FFFFFF -p BOTTOM_RIGHT -o 0.7\n");
    }
}