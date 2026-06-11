package back.incendios.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResponse {
    private String id;
    private LocalDateTime createdAt;
    private Double latitud;
    private Double longitud;
    private String direccion;
    private String comuna;
    private String descripcion;
    private String fotoUrl;
    private Integer nivelGravedad;
    private Double confianzaIa;
    private String metodoClasificacion;
    private String estado;
    private String reportadoPorNombre;
    private String atendidoPorNombre;
}
