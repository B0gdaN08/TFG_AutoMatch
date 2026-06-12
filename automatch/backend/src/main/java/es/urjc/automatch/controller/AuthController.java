package es.urjc.automatch.controller;

import es.urjc.automatch.dto.AuthDtos.AuthResponse;
import es.urjc.automatch.dto.AuthDtos.LoginRequest;
import es.urjc.automatch.dto.AuthDtos.RegisterRequest;
import es.urjc.automatch.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión de usuarios")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar usuario", description = "Crea una cuenta nueva y devuelve el token de acceso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de registro inválidos o email ya en uso")
    })
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales y devuelve el token JWT de acceso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas"),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos fallidos, cuenta bloqueada temporalmente")
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest.getRemoteAddr());
    }
}
