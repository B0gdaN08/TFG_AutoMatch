package es.urjc.automatch.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/** Lectura del usuario autenticado (si lo hay) a partir del contexto de seguridad. */
public final class AuthUtils {

    private AuthUtils() {
    }

    /** Email del usuario autenticado, o null si la petición es anónima. */
    public static String emailOrNull(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }

    public static boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
