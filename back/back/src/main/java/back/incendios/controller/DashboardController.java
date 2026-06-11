package back.incendios.controller;

import back.incendios.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final ReporteRepository reporteRepository;

    @GetMapping("/stats")
    public ResponseEntity<List<Map<String, Object>>> getStats() {
        List<Object[]> rows = reporteRepository.findEstadisticasDiarias();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("fecha", row[0] != null ? row[0].toString() : null);
            stat.put("total", row[1]);
            stat.put("gravedadPromedio", row[2]);
            stat.put("pendientes", row[3]);
            stat.put("enAtencion", row[4]);
            stat.put("extinguidos", row[5]);
            stat.put("criticos", row[6]);
            result.add(stat);
        }

        return ResponseEntity.ok(result);
    }
}
