package es.urjc.automatch.exception;

import es.urjc.automatch.service.LoginAttemptService.TooManyAttemptsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException e) {
        return body(e.getMessage(), "Petición no válida");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Petición no válida");
        return body(message, "Petición no válida");
    }

    /** Cuerpo de la peticion ilegible (JSON malformado, enum invalido...), parametro o tipo incorrecto en la URL. */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badInput(Exception e) {
        return body(null, "Petición no válida");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> uploadTooLarge(MaxUploadSizeExceededException e) {
        return body(null, "La imagen supera el tamaño máximo de 5MB");
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException e) {
        return body(e.getMessage(), "No encontrado");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> forbidden(AccessDeniedException e) {
        return body(e.getMessage(), "No tienes permisos para esta acción");
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, String> tooManyAttempts(TooManyAttemptsException e) {
        return body(e.getMessage(), "Demasiados intentos. Inténtalo de nuevo más tarde.");
    }

    /** Credenciales incorrectas en el login. */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> badCredentials(BadCredentialsException e) {
        return body(e.getMessage(), "Email o contraseña incorrectos");
    }

    /** Ruta inexistente: 404 limpio, sin stack trace en el log. */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> routeNotFound(NoResourceFoundException e) {
        return body(null, "No encontrado");
    }

    /** Metodo HTTP no soportado en una ruta que existe (p. ej. PATCH). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Map<String, String> methodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return body(null, "Método no permitido");
    }

    /** Content-Type no soportado (p. ej. XML en un endpoint JSON). */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Map<String, String> mediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return body(null, "Tipo de contenido no soportado");
    }

    /** Cualquier otro error no controlado: no se filtra al cliente, solo se registra en el log. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> internalError(Exception e) {
        log.error("Error interno no controlado", e);
        return Map.of("message", "Error interno del servidor");
    }

    private Map<String, String> body(String message, String fallback) {
        return Map.of("message", message != null ? message : fallback);
    }
}
