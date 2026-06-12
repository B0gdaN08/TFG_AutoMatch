package es.urjc.automatch.service;

import es.urjc.automatch.repository.CarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;

/** Limpieza periodica de fotos en /uploads que ya no estan asociadas a ningun coche. */
@Service
public class ImageCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ImageCleanupService.class);
    private static final long MIN_AGE_HOURS = 24;

    private final CarRepository carRepository;
    private final ImageStorageService imageStorageService;

    public ImageCleanupService(CarRepository carRepository, ImageStorageService imageStorageService) {
        this.carRepository = carRepository;
        this.imageStorageService = imageStorageService;
    }

    /** Cada dia a las 4:00, borra los ficheros de /uploads con mas de 24h que no referencia ningun coche. */
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupOrphanedImages() {
        Set<String> referenced = carRepository.findAll().stream()
                .flatMap(car -> car.getImageUrls().stream())
                .collect(Collectors.toSet());

        Instant cutoff = Instant.now().minus(MIN_AGE_HOURS, ChronoUnit.HOURS);

        try (DirectoryStream<Path> files = Files.newDirectoryStream(imageStorageService.getUploadDir())) {
            for (Path file : files) {
                if (Files.isDirectory(file)) {
                    continue;
                }
                String url = "/uploads/" + file.getFileName();
                if (referenced.contains(url)) {
                    continue;
                }
                if (Files.getLastModifiedTime(file).toInstant().isBefore(cutoff)) {
                    Files.delete(file);
                    log.info("Imagen huerfana eliminada: {}", file.getFileName());
                }
            }
        } catch (IOException e) {
            log.warn("No se pudo limpiar el directorio de subidas", e);
        }
    }
}
