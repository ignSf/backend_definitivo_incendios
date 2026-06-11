package back.incendios.controller;

import back.incendios.DTO.request.ActualizarEstadoRequest;
import back.incendios.DTO.request.CrearReporteRequest;
import back.incendios.DTO.response.ReporteResponse;
import back.incendios.service.ReporteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes de Incendios", description = "CRUD de reportes de incendios con clasificación IA y geolocalización")
public class ReporteController {

    private final ReporteService reporteService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear reporte", description = "Crea un nuevo reporte de incendio. Opcionalmente se adjunta una foto que será clasificada por IA para determinar la gravedad.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reporte creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos del reporte inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<ReporteResponse> crear(
            @Parameter(description = "JSON con los datos del reporte (latitud, longitud, dirección, comuna, descripción)")
            @RequestPart("datos") String datosJson,
            @Parameter(description = "Foto del incendio (opcional, max 10MB)")
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @AuthenticationPrincipal UserDetails user) throws Exception {

        CrearReporteRequest datos = objectMapper.readValue(datosJson, CrearReporteRequest.class);
        ReporteResponse reporte = reporteService.crear(datos, foto, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(reporte);
    }

    @GetMapping
    @Operation(summary = "Listar reportes", description = "Lista todos los reportes. Si se pasa el parámetro 'estado', filtra por ese estado; si no, muestra los reportes activos (no extinguidos).")
    @ApiResponse(responseCode = "200", description = "Lista de reportes")
    public ResponseEntity<List<ReporteResponse>> listar(
            @Parameter(description = "Filtrar por estado: PENDIENTE, EN_ATENCION, CONTROLADO, EXTINGUIDO")
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(reporteService.listar(estado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reporte por ID", description = "Retorna los detalles completos de un reporte específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte encontrado"),
            @ApiResponse(responseCode = "404", description = "Reporte no encontrado")
    })
    public ResponseEntity<ReporteResponse> obtener(
            @Parameter(description = "UUID del reporte") @PathVariable UUID id) {
        return ResponseEntity.ok(reporteService.obtenerPorId(id));
    }

    @GetMapping("/mis-reportes")
    @Operation(summary = "Mis reportes", description = "Lista los reportes creados por el usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reportes del usuario"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<List<ReporteResponse>> misReportes(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reporteService.misReportes(user.getUsername()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRIGADISTA')")
    @Operation(summary = "Actualizar estado", description = "Cambia el estado de un reporte (solo ADMIN o BRIGADISTA). Emite un evento WebSocket.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere rol ADMIN o BRIGADISTA)"),
            @ApiResponse(responseCode = "404", description = "Reporte no encontrado")
    })
    public ResponseEntity<ReporteResponse> actualizarEstado(
            @Parameter(description = "UUID del reporte") @PathVariable UUID id,
            @Valid @RequestBody ActualizarEstadoRequest request) {
        return ResponseEntity.ok(reporteService.actualizarEstado(id, request));
    }

    @GetMapping("/zona")
    @Operation(summary = "Buscar por zona", description = "Busca reportes dentro de un bounding box (viewport del mapa). Retorna reportes de los últimos 30 días.")
    @ApiResponse(responseCode = "200", description = "Lista de reportes en la zona")
    public ResponseEntity<List<ReporteResponse>> porZona(
            @Parameter(description = "Latitud norte del bounding box") @RequestParam double north,
            @Parameter(description = "Latitud sur del bounding box") @RequestParam double south,
            @Parameter(description = "Longitud este del bounding box") @RequestParam double east,
            @Parameter(description = "Longitud oeste del bounding box") @RequestParam double west) {
        return ResponseEntity.ok(reporteService.buscarEnZona(north, south, east, west));
    }

    @GetMapping("/cercanos")
    @Operation(summary = "Buscar cercanos", description = "Busca reportes PENDIENTES o EN_ATENCION dentro de un radio en metros desde un punto")
    @ApiResponse(responseCode = "200", description = "Lista de reportes cercanos")
    public ResponseEntity<List<ReporteResponse>> cercanos(
            @Parameter(description = "Latitud del punto central") @RequestParam double lat,
            @Parameter(description = "Longitud del punto central") @RequestParam double lng,
            @Parameter(description = "Radio de búsqueda en metros (default: 5000)") @RequestParam(defaultValue = "5000") double radio) {
        return ResponseEntity.ok(reporteService.buscarCercanos(lat, lng, radio));
    }
}
