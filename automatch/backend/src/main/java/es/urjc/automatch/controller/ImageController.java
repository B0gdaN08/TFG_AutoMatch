package es.urjc.automatch.controller;

import es.urjc.automatch.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/** Subida de fotos para anuncios de coches: guarda el fichero y devuelve su URL publica. */
@RestController
@RequestMapping("/api/cars/images")
@Tag(name = "Imágenes", description = "Subida de fotos para los anuncios de coches")
public class ImageController {

    private final ImageStorageService imageStorageService;

    public ImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @Operation(summary = "Subir imagen", description = "Sube una foto (JPEG, PNG, GIF o WEBP) y devuelve su URL pública")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Imagen subida correctamente"),
            @ApiResponse(responseCode = "400", description = "El fichero no es una imagen válida")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return Map.of("url", imageStorageService.store(file));
    }
}
