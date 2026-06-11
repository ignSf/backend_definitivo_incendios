package back.incendios.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "brigadistas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Brigadista {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    @Column(name = "zona_asignada")
    private String zonaAsignada;

    @Builder.Default
    private Boolean disponible = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
