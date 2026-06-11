package back.incendios.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String id;
    private String email;
    private String nombre;
    private String rol;
}
