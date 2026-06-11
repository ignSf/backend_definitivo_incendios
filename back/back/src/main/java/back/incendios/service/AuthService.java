package back.incendios.service;

import back.incendios.DTO.request.LoginRequest;
import back.incendios.DTO.request.RegisterRequest;
import back.incendios.DTO.response.AuthResponse;
import back.incendios.config.JwtUtil;
import back.incendios.model.Usuario;
import back.incendios.model.enums.RolUsuario;
import back.incendios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .telefono(request.getTelefono())
                .rol(RolUsuario.CIUDADANO)
                .build();

        usuario = usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .id(usuario.getId().toString())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().name());

        return AuthResponse.builder()
                .token(token)
                .id(usuario.getId().toString())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .build();
    }

    public AuthResponse me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return AuthResponse.builder()
                .id(usuario.getId().toString())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .build();
    }
}
