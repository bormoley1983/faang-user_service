package school.faang.user_service.validation.image;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class ImageFileValidatorTest {

    private final ImageFileValidator validator = new ImageFileValidator();

    @Test
    void isValid_whenNullFile_returnsFalse() {
        // Act
        boolean result = validator.isValid(null, null);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenEmptyFile_returnsFalse() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[0]);

        // Act
        boolean result = validator.isValid(file, null);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenPng_returnsTrue() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});

        // Act
        boolean result = validator.isValid(file, null);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isValid_whenJpeg_returnsTrue() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1});

        // Act
        boolean result = validator.isValid(file, null);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isValid_whenJpg_returnsTrue() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpg", new byte[]{1});

        // Act
        boolean result = validator.isValid(file, null);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isValid_whenGif_returnsFalse() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "a.gif", "image/gif", new byte[]{1});

        // Act
        boolean result = validator.isValid(file, null);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenNullContentType_returnsFalse() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "a.bin", null, new byte[]{1});

        // Act
        boolean result = validator.isValid(file, null);

        // Assert
        assertThat(result).isFalse();
    }
}
