package back.incendios.DTO.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearReporteRequest {
    @NotNull
    private Double latitud;

    @NotNull
    private Double longitud;

    private String direccion;
    private String comuna;

    @Size(max = 2000)
    private String descripcion;
}
