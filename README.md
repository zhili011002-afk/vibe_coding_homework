# Image Watermark Tool

A Java command-line tool that adds date watermarks to images based on their EXIF data.

## Features

- 读取图片文件的EXIF信息中的拍摄时间
- 将年月日作为水印添加到图片上
- 支持自定义字体大小、颜色和位置
- 批量处理目录中的所有图片
- 自动创建带水印的新图片文件
- 支持多种图片格式 (JPG, JPEG, PNG, TIFF, BMP, GIF)

## Requirements

- Java 8 or higher
- Maven 3.6+ (for building)

## Building

```bash
mvn clean package
```

This will create two JAR files in the `target/` directory:
- `image-watermark-1.0.0.jar` - Basic JAR without dependencies
- `image-watermark-1.0.0-jar-with-dependencies.jar` - Executable JAR with all dependencies

## Usage

### Basic Syntax

```bash
java -jar target/image-watermark-1.0.0-jar-with-dependencies.jar -i <input_path> [options]
```

### Options

| Option | Long Option | Description | Default |
|--------|-------------|-------------|----------|
| `-i` | `--input` | Input image file or directory path | Required |
| `-s` | `--size` | Font size for watermark | 24 |
| `-c` | `--color` | Watermark color in hex format | #FFFFFF (white) |
| `-p` | `--position` | Watermark position | BOTTOM_RIGHT |
| `-o` | `--opacity` | Watermark opacity (0.0-1.0) | 0.8 |
| `-h` | `--help` | Show help message | - |

### Position Options

- `TOP_LEFT` - 左上角
- `TOP_RIGHT` - 右上角
- `BOTTOM_LEFT` - 左下角
- `BOTTOM_RIGHT` - 右下角 (默认)
- `CENTER` - 居中

### Examples

#### Process a single image file:
```bash
java -jar target/image-watermark-1.0.0-jar-with-dependencies.jar -i photo.jpg
```

#### Process all images in a directory:
```bash
java -jar target/image-watermark-1.0.0-jar-with-dependencies.jar -i /path/to/photos
```

#### Custom watermark settings:
```bash
# Red watermark, size 32, top-left position
java -jar target/image-watermark-1.0.0-jar-with-dependencies.jar -i photos/ -s 32 -c #FF0000 -p TOP_LEFT

# White watermark, size 28, bottom-right, 70% opacity
java -jar target/image-watermark-1.0.0-jar-with-dependencies.jar -i photos/ -s 28 -c #FFFFFF -p BOTTOM_RIGHT -o 0.7
```

## Output

处理后的图片将保存在原目录的子目录中，目录名为 `<原目录名>_watermark`。

For example:
- Input directory: `/photos`
- Output directory: `/photos/photos_watermark`

Watermarked images will have `_watermarked` appended to their filename:
- Original: `photo.jpg`
- Watermarked: `photo_watermarked.jpg`

## How it Works

1. **EXIF Reading**: The tool reads EXIF metadata from image files to extract the date the photo was taken
2. **Date Formatting**: Extracts year, month, and day in YYYY-MM-DD format
3. **Fallback**: If no EXIF date is available, uses the file's last modified date
4. **Watermarking**: Applies the date as a text watermark with specified styling
5. **Output**: Saves the watermarked image to a new directory

## Supported Image Formats

- JPEG (.jpg, .jpeg)
- PNG (.png)
- TIFF (.tiff, .tif)
- BMP (.bmp)
- GIF (.gif)

## Error Handling

The tool includes comprehensive error handling for:
- Invalid file paths
- Unsupported image formats
- File permission issues
- Invalid configuration parameters
- EXIF reading errors

## Project Structure

```
src/main/java/com/vibe/watermark/
├── ImageWatermarkTool.java      # Main application class
├── ExifReader.java              # EXIF data extraction
├── WatermarkApplier.java        # Image watermarking logic
├── WatermarkConfig.java         # Configuration settings
├── WatermarkPosition.java       # Position enumeration
├── ImageProcessor.java          # File processing logic
└── ValidationUtils.java         # Input validation utilities
```

## Development

### Running Tests
```bash
mvn test
```

### Building without Tests
```bash
mvn clean package -DskipTests
```

### Quiet Build
```bash
mvn clean package -q
```

## License

This project is part of a coding homework assignment.

## Version History

- v1.0.0 - Initial release with core functionality
