package back.incendios.model;

import back.incendios.model.enums.EstadoReporte;
import back.incendios.model.enums.MetodoIA;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reportes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point ubicacion;

    private String direccion;
    private String comuna;
    private String descripcion;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "nivel_gravedad")
    private Integer nivelGravedad;

    @Column(name = "confianza_ia")
    private Double confianzaIa;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "metodo_clasificacion", columnDefinition = "metodo_ia")
    private MetodoIA metodoClasificacion;

    @Column(name = "clasificado_at")
    private LocalDateTime clasificadoAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "estado_reporte")
    @Builder.Default
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportado_por")
    private Usuario reportadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendido_por")
    private Brigadista atendidoPor;
}
