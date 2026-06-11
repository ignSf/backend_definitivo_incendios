package back.incendios.repository;

import back.incendios.model.Brigadista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BrigadistaRepository extends JpaRepository<Brigadista, UUID> {
    Optional<Brigadista> findByUsuarioId(UUID userId);
}
