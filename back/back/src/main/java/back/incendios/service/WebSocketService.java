package back.incendios.service;

import back.incendios.DTO.response.ReporteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void emitirNuevoReporte(ReporteResponse reporte) {
        messagingTemplate.convertAndSend("/topic/reportes", reporte);
    }

    public void emitirAlertaCritica(ReporteResponse reporte) {
        messagingTemplate.convertAndSend("/topic/alertas", reporte);
    }

    public void emitirCambioEstado(String reporteId, String nuevoEstado) {
        messagingTemplate.convertAndSend("/topic/estados",
                java.util.Map.of("id", reporteId, "estado", nuevoEstado));
    }
}
