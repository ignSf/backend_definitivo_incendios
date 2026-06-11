package back.incendios.controller;

import back.incendios.DTO.request.ActualizarEstadoRequest;
import back.incendios.DTO.request.CrearReporteRequest;
import back.incendios.DTO.response.ReporteResponse;
import back.incendios.service.ReporteService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ReporteController {

    private final ReporteService reporteService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReporteResponse> crear(
            @RequestPart("datos") String datosJson,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @AuthenticationPrincipal UserDetails user) throws Exception {

        CrearReporteRequest datos = objectMapper.readValue(datosJson, CrearReporteRequest.class);
        ReporteResponse reporte = reporteService.crear(datos, foto, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(reporte);
    }

    @GetMapping
    public ResponseEntity<List<ReporteResponse>> listar(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(reporteService.listar(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(reporteService.obtenerPorId(id));
    }

    @GetMapping("/mis-reportes")
    public ResponseEntity<List<ReporteResponse>> misReportes(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reporteService.misReportes(user.getUsername()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRIGADISTA')")
    public ResponseEntity<ReporteResponse> actualizarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarEstadoRequest request) {
        return ResponseEntity.ok(reporteService.actualizarEstado(id, request));
    }

    @GetMapping("/zona")
    public ResponseEntity<List<ReporteResponse>> porZona(
            @RequestParam double north,
            @RequestParam double south,
            @RequestParam double east,
            @RequestParam double west) {
        return ResponseEntity.ok(reporteService.buscarEnZona(north, south, east, west));
    }

    @GetMapping("/cercanos")
    public ResponseEntity<List<ReporteResponse>> cercanos(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5000") double radio) {
        return ResponseEntity.ok(reporteService.buscarCercanos(lat, lng, radio));
    }
}
