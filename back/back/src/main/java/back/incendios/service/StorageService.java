package back.incendios.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    @Value("${app.uploads.path}")
    private String uploadsPath;

    public String subirFoto(MultipartFile file) {
        try {
            Path uploadDir = Paths.get(uploadsPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path filePath = uploadDir.resolve(filename);

            Files.write(filePath, file.getBytes());
            log.info("Foto guardada: {}", filePath);

            return "/uploads/" + filename;
        } catch (IOException e) {
            log.error("Error al subir foto: {}", e.getMessage());
            throw new RuntimeException("Error al subir la foto", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}
