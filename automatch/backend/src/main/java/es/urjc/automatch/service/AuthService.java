package es.urjc.automatch.service;

import es.urjc.automatch.dto.AuthDtos.AuthResponse;
import es.urjc.automatch.dto.AuthDtos.LoginRequest;
import es.urjc.automatch.dto.AuthDtos.RegisterRequest;
import es.urjc.automatch.model.User;
import es.urjc.automatch.repository.UserRepository;
import es.urjc.automatch.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                        LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        }
        User user = new User(email,
                passwordEncoder.encode(request.password()),
                request.name(),
                "ROLE_USER");
        userRepository.save(user);
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest request, String ip) {
        String email = normalizeEmail(request.email());
        loginAttemptService.checkAllowed(email, ip);

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty() || !passwordEncoder.matches(request.password(), user.get().getPassword())) {
            loginAttemptService.onFailedAttempt(email, ip);
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }
        loginAttemptService.onSuccessfulLogin(email, ip);
        return toResponse(user.get());
    }

    /**
     * Los emails se guardan y comparan siempre en minusculas: "Demo@x.es" y
     * "demo@x.es" son la misma cuenta (la comparacion de PostgreSQL es sensible
     * a mayusculas y sin esto se podrian crear cuentas duplicadas).
     */
    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResponse toResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole());
    }
}
