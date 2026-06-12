package es.urjc.automatch.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storesValidJpegImage() throws IOException {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        byte[] jpegBytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", jpegBytes);

        String url = service.store(file);

        assertThat(url).startsWith("/uploads/").endsWith(".jpg");
    }

    @Test
    void rejectsTextFileRenamedAsJpeg() throws IOException {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        byte[] textBytes = "esto no es una imagen".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", textBytes);

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsUnsupportedContentType() throws IOException {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "foto.bmp", "image/bmp", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
