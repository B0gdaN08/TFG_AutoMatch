package es.urjc.automatch.service;

import es.urjc.automatch.dto.AuthDtos.AuthResponse;
import es.urjc.automatch.dto.AuthDtos.LoginRequest;
import es.urjc.automatch.dto.AuthDtos.RegisterRequest;
import es.urjc.automatch.model.User;
import es.urjc.automatch.repository.UserRepository;
import es.urjc.automatch.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email o contrasena incorrectos"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Email o contrasena incorrectos");
        }
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole());
    }
}
