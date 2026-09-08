package school.faang.user_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleDataValidationException() {
        DataValidationException exception = new DataValidationException(ErrorMessages.BAD_REQUEST.getMessage());

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleException(exception);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals("Bad Request", Objects.requireNonNull(response.getBody()).get("error")),
                () -> assertEquals(ErrorMessages.BAD_REQUEST.getMessage(), Objects.requireNonNull(response.getBody()).get("message"))
        );
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleException(exception);

        assertAll(
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals("Bad Request", Objects.requireNonNull(response.getBody()).get("error")),
                () -> assertEquals("Invalid input", Objects.requireNonNull(response.getBody()).get("message"))
        );
    }

    @Test
    void testHandleNoSuchElementException() {
        NoSuchElementException exception = new NoSuchElementException(ErrorMessages.NOT_FOUND.getMessage());

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleException(exception);

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals("Not Found", Objects.requireNonNull(response.getBody()).get("error")),
                () -> assertEquals(ErrorMessages.NOT_FOUND.getMessage(), Objects.requireNonNull(response.getBody()).get("message"))
        );
    }

    @Test
    void testHandleIllegalStateException() {
        IllegalStateException exception = new IllegalStateException("Invalid state");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleException(exception);

        assertAll(
                () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
                () -> assertEquals("Conflict", Objects.requireNonNull(response.getBody()).get("error")),
                () -> assertEquals("Invalid state", Objects.requireNonNull(response.getBody()).get("message"))
        );
    }

    @Test
    void testHandleUnknownException() {
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleException(exception);

        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertEquals("Internal Server Error", Objects.requireNonNull(response.getBody()).get("error")),
                () -> assertEquals("Unexpected error", Objects.requireNonNull(response.getBody()).get("message"))
        );
    }

    @Test
    void testHandleNoResourceFoundException() {
        NoResourceFoundException exception = new NoResourceFoundException(
                HttpMethod.GET, "No static resource", "/does-not-exist");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleNoResourceFound(exception);

        assertAll(
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
                () -> assertEquals("Not Found", Objects.requireNonNull(response.getBody()).get("error")),
                () -> assertTrue(Objects.requireNonNull(response.getBody()).get("message").contains("/does-not-exist"))
        );
    }
}
