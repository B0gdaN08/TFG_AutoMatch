package es.urjc.automatch.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proteccion basica contra fuerza bruta en el login: cuenta los intentos
 * fallidos por combinacion de email + IP dentro de una ventana de tiempo.
 * Al superar el limite, bloquea nuevos intentos hasta que expire la ventana.
 */
@Component
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 10;
    public static final long WINDOW_MINUTES = 15;

    private final ConcurrentHashMap<String, Attempts> attemptsByKey = new ConcurrentHashMap<>();

    /** Lanza TooManyAttemptsException si esta combinacion email+IP esta bloqueada. */
    public void checkAllowed(String email, String ip) {
        Attempts attempts = attemptsByKey.get(key(email, ip));
        if (attempts != null && attempts.isBlocked()) {
            throw new TooManyAttemptsException(
                    "Demasiados intentos fallidos. Inténtalo de nuevo en unos minutos.");
        }
    }

    public void onFailedAttempt(String email, String ip) {
        attemptsByKey.compute(key(email, ip), (k, current) -> {
            if (current == null || current.isExpired()) {
                return new Attempts(1, Instant.now());
            }
            return new Attempts(current.count + 1, current.windowStart);
        });
    }

    public void onSuccessfulLogin(String email, String ip) {
        attemptsByKey.remove(key(email, ip));
    }

    private String key(String email, String ip) {
        return email.toLowerCase() + "|" + ip;
    }

    private static final class Attempts {
        final int count;
        final Instant windowStart;

        Attempts(int count, Instant windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }

        boolean isExpired() {
            return Instant.now().isAfter(windowStart.plus(WINDOW_MINUTES, ChronoUnit.MINUTES));
        }

        boolean isBlocked() {
            return !isExpired() && count >= MAX_ATTEMPTS;
        }
    }

    public static class TooManyAttemptsException extends RuntimeException {
        public TooManyAttemptsException(String message) {
            super(message);
        }
    }
}
