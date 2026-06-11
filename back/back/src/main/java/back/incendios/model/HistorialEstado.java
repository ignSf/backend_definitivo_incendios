package back.incendios.model;

import back.incendios.model.enums.EstadoReporte;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historial_estados")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id")
    private Reporte reporte;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", columnDefinition = "estado_reporte")
    private EstadoReporte estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, columnDefinition = "estado_reporte")
    private EstadoReporte estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cambiado_por")
    private Usuario cambiadoPor;

    private String comentario;
}
