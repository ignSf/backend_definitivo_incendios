package back.incendios.DTO.request;

import back.incendios.model.enums.EstadoReporte;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarEstadoRequest {
    @NotNull
    private EstadoReporte estado;

    private String comentario;
}
