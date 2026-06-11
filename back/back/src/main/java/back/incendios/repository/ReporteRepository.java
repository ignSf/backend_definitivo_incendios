package back.incendios.repository;

import back.incendios.model.Reporte;
import back.incendios.model.enums.EstadoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReporteRepository extends JpaRepository<Reporte, UUID> {

    List<Reporte> findByEstadoOrderByCreatedAtDesc(EstadoReporte estado);

    List<Reporte> findByReportadoPorIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT r FROM Reporte r WHERE r.estado <> 'EXTINGUIDO' ORDER BY r.nivelGravedad DESC, r.createdAt DESC")
    List<Reporte> findActivos();

    // Reportes dentro de un bounding box (viewport del mapa)
    @Query(value = """
        SELECT * FROM reportes
        WHERE ubicacion && ST_MakeEnvelope(:west, :south, :east, :north, 4326)
        AND created_at > NOW() - INTERVAL '30 days'
        ORDER BY nivel_gravedad DESC NULLS LAST
        """, nativeQuery = true)
    List<Reporte> findEnZona(
            @Param("north") double north,
            @Param("south") double south,
            @Param("east") double east,
            @Param("west") double west
    );

    // Reportes dentro de un radio en metros
    @Query(value = """
        SELECT * FROM reportes
        WHERE ST_DWithin(
            ubicacion::geography,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
            :radioMetros
        )
        AND estado IN ('PENDIENTE', 'EN_ATENCION')
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<Reporte> findCercanos(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radioMetros") double radioMetros
    );

    // Estadísticas diarias para dashboard
    @Query(value = """
        SELECT DATE(created_at) as fecha,
               COUNT(*) as total,
               COALESCE(AVG(nivel_gravedad), 0) as gravedad_promedio,
               COUNT(*) FILTER (WHERE estado = 'PENDIENTE') as pendientes,
               COUNT(*) FILTER (WHERE estado = 'EN_ATENCION') as en_atencion,
               COUNT(*) FILTER (WHERE estado = 'EXTINGUIDO') as extinguidos,
               COUNT(*) FILTER (WHERE nivel_gravedad >= 4) as criticos
        FROM reportes
        GROUP BY DATE(created_at)
        ORDER BY fecha DESC
        LIMIT 30
        """, nativeQuery = true)
    List<Object[]> findEstadisticasDiarias();
}
