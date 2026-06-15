package dae.me.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class ImageService {

    private static final Path IMAGE_DIR = Path.of("data", "images");

    public ImageService() {
        try {
            Files.createDirectories(IMAGE_DIR);
        } catch (IOException e) {
            log.error("Could not create image directory", e);
        }
    }

    public String downloadImage(String imageUrl, Long malId) {
        if (imageUrl == null || imageUrl.isBlank()) return null;

        String extension = extractExtension(imageUrl);
        String fileName = "anime_" + malId + "." + extension;
        Path targetPath = IMAGE_DIR.resolve(fileName);

        if (Files.exists(targetPath)) {
            return targetPath.toString();
        }

        try {
            URI uri = URI.create(imageUrl);
            try (InputStream in = uri.toURL().openStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                return targetPath.toString();
            }
        } catch (Exception e) {
            log.error("Failed to download image from {}", imageUrl, e);
            return null;
        }
    }

    private String extractExtension(String url) {
        String lower = url.toLowerCase();
        if (lower.contains(".jpg") || lower.contains(".jpeg")) return "jpg";
        if (lower.contains(".png")) return "png";
        if (lower.contains(".webp")) return "webp";
        return "jpg";
    }
}
