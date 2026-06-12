package es.urjc.automatch.config;

import es.urjc.automatch.service.ImageStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Sirve las fotos subidas en /uploads/** directamente desde el disco. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ImageStorageService imageStorageService;

    public WebConfig(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = imageStorageService.getUploadDir().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
