package back.incendios.repository;

import back.incendios.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertaRepository extends JpaRepository<Alerta, UUID> {
}
