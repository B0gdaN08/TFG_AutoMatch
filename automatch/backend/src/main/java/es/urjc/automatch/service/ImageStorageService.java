package es.urjc.automatch.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/** Guarda y sirve las fotos subidas para los anuncios de coches en disco local. */
@Service
public class ImageStorageService {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    // Cabeceras ("magic bytes") que identifican el formato real del fichero,
    // independientemente de lo que declare el Content-Type del cliente.
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] GIF_SIGNATURE = "GIF8".getBytes();
    private static final byte[] RIFF_SIGNATURE = "RIFF".getBytes();
    private static final byte[] WEBP_SIGNATURE = "WEBP".getBytes();

    private final Path uploadDir;

    public ImageStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo esta vacio");
        }
        String declaredExtension = EXTENSIONS.get(file.getContentType());
        if (declaredExtension == null) {
            throw new IllegalArgumentException("Formato de imagen no soportado (usa JPG, PNG, WEBP o GIF)");
        }

        byte[] header = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(header, 0, header.length);
        }
        String realExtension = detectExtension(header, read);
        if (realExtension == null || !realExtension.equals(declaredExtension)) {
            throw new IllegalArgumentException("El archivo no es una imagen valida (JPG, PNG, WEBP o GIF)");
        }

        String filename = UUID.randomUUID() + realExtension;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        }
        return "/uploads/" + filename;
    }

    /** Identifica el formato real de la imagen a partir de sus primeros bytes. */
    private String detectExtension(byte[] header, int length) {
        if (startsWith(header, length, JPEG_SIGNATURE)) {
            return ".jpg";
        }
        if (startsWith(header, length, PNG_SIGNATURE)) {
            return ".png";
        }
        if (startsWith(header, length, GIF_SIGNATURE) && length >= 6
                && (header[4] == '7' || header[4] == '9')) {
            return ".gif";
        }
        if (startsWith(header, length, RIFF_SIGNATURE) && length >= 12
                && regionMatches(header, 8, WEBP_SIGNATURE)) {
            return ".webp";
        }
        return null;
    }

    private boolean startsWith(byte[] data, int length, byte[] prefix) {
        return length >= prefix.length && regionMatches(data, 0, prefix);
    }

    private boolean regionMatches(byte[] data, int offset, byte[] expected) {
        if (data.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (data[offset + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    /** Borra el fichero asociado a una URL "/uploads/xxx"; ignora rutas externas o invalidas. */
    public void delete(String url) {
        if (url == null || !url.startsWith("/uploads/")) {
            return;
        }
        String filename = url.substring("/uploads/".length());
        if (filename.isBlank() || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            return;
        }
        try {
            Files.deleteIfExists(uploadDir.resolve(filename));
        } catch (IOException ignored) {
        }
    }
}
