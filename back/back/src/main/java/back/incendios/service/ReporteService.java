package back.incendios.service;

import back.incendios.DTO.request.ActualizarEstadoRequest;
import back.incendios.DTO.request.CrearReporteRequest;
import back.incendios.DTO.response.ReporteResponse;
import back.incendios.model.Reporte;
import back.incendios.model.Usuario;
import back.incendios.model.enums.EstadoReporte;
import back.incendios.model.enums.MetodoIA;
import back.incendios.repository.ReporteRepository;
import back.incendios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;
    private final WebSocketService webSocketService;
    private final IAService iaService;

    @Transactional
    public ReporteResponse crear(CrearReporteRequest request, MultipartFile foto, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Subir foto
        String fotoUrl = null;
        if (foto != null && !foto.isEmpty()) {
            fotoUrl = storageService.subirFoto(foto);
        }

        // 2. Clasificar con IA
        Integer nivel = null;
        Double confianza = null;
        MetodoIA metodo = null;
        if (fotoUrl != null) {
            try {
                Map<String, Object> clasificacion = iaService.clasificarImagen(fotoUrl);
                nivel = (Integer) clasificacion.get("nivel");
                confianza = (Double) clasificacion.get("confianza");
                metodo = MetodoIA.CNN;
            } catch (Exception e) {
                log.warn("IA no disponible: {}", e.getMessage());
            }
        }

        // 3. Crear entidad
        Reporte reporte = Reporte.builder()
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .direccion(request.getDireccion())
                .comuna(request.getComuna())
                .descripcion(request.getDescripcion())
                .fotoUrl(fotoUrl)
                .nivelGravedad(nivel)
                .confianzaIa(confianza)
                .metodoClasificacion(metodo)
                .reportadoPor(usuario)
                .build();

        reporte = reporteRepository.save(reporte);
        ReporteResponse response = toResponse(reporte);

        // 4. Emitir evento WebSocket
        webSocketService.emitirNuevoReporte(response);

        // 5. Si es grave, emitir alerta
        if (nivel != null && nivel >= 4) {
            webSocketService.emitirAlertaCritica(response);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> listar(String estado) {
        List<Reporte> reportes;
        if (estado != null && !estado.isEmpty()) {
            reportes = reporteRepository.findByEstadoOrderByCreatedAtDesc(
                    EstadoReporte.valueOf(estado.toUpperCase()));
        } else {
            reportes = reporteRepository.findActivos();
        }
        return reportes.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReporteResponse obtenerPorId(UUID id) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
        return toResponse(reporte);
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> misReportes(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return reporteRepository.findByReportadoPorIdOrderByCreatedAtDesc(usuario.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ReporteResponse actualizarEstado(UUID id, ActualizarEstadoRequest request) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

        reporte.setEstado(request.getEstado());
        reporte = reporteRepository.save(reporte);

        ReporteResponse response = toResponse(reporte);
        webSocketService.emitirCambioEstado(id.toString(), request.getEstado().name());

        return response;
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> buscarEnZona(double north, double south, double east, double west) {
        return reporteRepository.findEnZona(north, south, east, west)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> buscarCercanos(double lat, double lng, double radioMetros) {
        return reporteRepository.findCercanos(lat, lng, radioMetros)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ReporteResponse toResponse(Reporte r) {
        return ReporteResponse.builder()
                .id(r.getId().toString())
                .createdAt(r.getCreatedAt())
                .latitud(r.getLatitud())
                .longitud(r.getLongitud())
                .direccion(r.getDireccion())
                .comuna(r.getComuna())
                .descripcion(r.getDescripcion())
                .fotoUrl(r.getFotoUrl())
                .nivelGravedad(r.getNivelGravedad())
                .confianzaIa(r.getConfianzaIa())
                .metodoClasificacion(r.getMetodoClasificacion() != null ? r.getMetodoClasificacion().name() : null)
                .estado(r.getEstado().name())
                .reportadoPorNombre(r.getReportadoPor() != null ? r.getReportadoPor().getNombre() : null)
                .atendidoPorNombre(r.getAtendidoPor() != null ? r.getAtendidoPor().getNombre() : null)
                .build();
    }
}
