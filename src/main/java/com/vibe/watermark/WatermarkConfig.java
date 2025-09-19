package com.vibe.watermark;

import java.awt.*;

/**
 * Configuration class for watermark settings.
 */
public class WatermarkConfig {
    private int fontSize = 24;
    private Color color = Color.WHITE;
    private WatermarkPosition position = WatermarkPosition.BOTTOM_RIGHT;
    private float opacity = 0.8f;
    private String fontName = "Arial";
    private int fontStyle = Font.BOLD;
    private int margin = 20; // Margin from edges

    // Constructors
    public WatermarkConfig() {
    }

    public WatermarkConfig(int fontSize, Color color, WatermarkPosition position, float opacity) {
        this.fontSize = fontSize;
        this.color = color;
        this.position = position;
        this.opacity = opacity;
    }

    // Getters and Setters
    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        if (fontSize > 0) {
            this.fontSize = fontSize;
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color != null) {
            this.color = color;
        }
    }

    public WatermarkPosition getPosition() {
        return position;
    }

    public void setPosition(WatermarkPosition position) {
        if (position != null) {
            this.position = position;
        }
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        if (opacity >= 0.0f && opacity <= 1.0f) {
            this.opacity = opacity;
        }
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        if (fontName != null && !fontName.trim().isEmpty()) {
            this.fontName = fontName;
        }
    }

    public int getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(int fontStyle) {
        this.fontStyle = fontStyle;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        if (margin >= 0) {
            this.margin = margin;
        }
    }

    /**
     * Creates a Font object based on the current configuration.
     */
    public Font createFont() {
        return new Font(fontName, fontStyle, fontSize);
    }

    /**
     * Creates a Color object with the specified opacity.
     */
    public Color createColorWithOpacity() {
        return new Color(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            Math.round(255 * opacity)
        );
    }

    @Override
    public String toString() {
        return String.format("WatermarkConfig{fontSize=%d, color=%s, position=%s, opacity=%.2f, fontName='%s'}",
                fontSize, color, position, opacity, fontName);
    }
}