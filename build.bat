@echo off
REM Build script for Image Watermark Tool

echo Building Image Watermark Tool...
mvn clean package -q

if %errorlevel% equ 0 (
    echo.
    echo Build successful!
    echo.
    echo Executable JAR: target\image-watermark-1.0.0-jar-with-dependencies.jar
    echo.
    echo Usage examples:
    echo   java -jar target\image-watermark-1.0.0-jar-with-dependencies.jar -i photos\
    echo   java -jar target\image-watermark-1.0.0-jar-with-dependencies.jar -i photo.jpg -s 32 -c #FF0000
    echo.
) else (
    echo Build failed!
    exit /b 1
)