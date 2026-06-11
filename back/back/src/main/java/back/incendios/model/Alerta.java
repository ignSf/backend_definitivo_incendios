package back.incendios.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alertas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id")
    private Reporte reporte;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String destinatario;

    private String mensaje;

    @Builder.Default
    private Boolean enviada = false;

    @Column(name = "enviada_at")
    private LocalDateTime enviadaAt;
}
