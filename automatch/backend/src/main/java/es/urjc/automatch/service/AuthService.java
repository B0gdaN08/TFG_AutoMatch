package es.urjc.automatch.service;

import es.urjc.automatch.dto.AuthDtos.AuthResponse;
import es.urjc.automatch.dto.AuthDtos.LoginRequest;
import es.urjc.automatch.dto.AuthDtos.RegisterRequest;
import es.urjc.automatch.model.User;
import es.urjc.automatch.repository.UserRepository;
import es.urjc.automatch.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        }
        User user = new User(request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                "ROLE_USER");
        userRepository.save(user);
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest request, String ip) {
        loginAttemptService.checkAllowed(request.email(), ip);

        Optional<User> user = userRepository.findByEmail(request.email());
        if (user.isEmpty() || !passwordEncoder.matches(request.password(), user.get().getPassword())) {
            loginAttemptService.onFailedAttempt(request.email(), ip);
            throw new IllegalArgumentException("Email o contrasena incorrectos");
        }
        loginAttemptService.onSuccessfulLogin(request.email(), ip);
        return toResponse(user.get());
    }

    private AuthResponse toResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole());
    }
}
