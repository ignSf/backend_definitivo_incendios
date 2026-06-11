package back.incendios.controller;

import back.incendios.DTO.request.LoginRequest;
import back.incendios.DTO.request.RegisterRequest;
import back.incendios.DTO.response.AuthResponse;
import back.incendios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de registro, login y perfil del usuario")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta de usuario ciudadano y retorna un token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de registro inválidos"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con email y contraseña, retorna un token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil", description = "Retorna los datos del usuario autenticado (requiere token JWT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil del usuario"),
            @ApiResponse(responseCode = "401", description = "No autenticado o token inválido")
    })
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(authService.me(user.getUsername()));
    }
}
