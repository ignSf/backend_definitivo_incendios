package back.incendios.service;

import back.incendios.DTO.request.ActualizarEstadoRequest;
import back.incendios.DTO.request.CrearReporteRequest;
import back.incendios.DTO.response.ReporteResponse;
import back.incendios.model.Reporte;
import back.incendios.model.Usuario;
import back.incendios.model.enums.EstadoReporte;
import back.incendios.model.enums.MetodoIA;
import back.incendios.repository.ReporteRepository;
import back.incendios.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private IAService iaService;

    @InjectMocks
    private ReporteService reporteService;

    // ====== Helpers ======

    private Usuario crearUsuario() {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hash123")
                .nombre("Juan Pérez")
                .build();
    }

    private Reporte crearReporte(Usuario usuario) {
        return Reporte.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .latitud(-33.45)
                .longitud(-70.66)
                .direccion("Calle Falsa 123")
                .comuna("Santiago")
                .descripcion("Incendio forestal en cerro")
                .fotoUrl("/uploads/foto.jpg")
                .nivelGravedad(3)
                .confianzaIa(85.5)
                .metodoClasificacion(MetodoIA.CNN)
                .estado(EstadoReporte.PENDIENTE)
                .reportadoPor(usuario)
                .build();
    }

    // ====== Test 1: Crear reporte sin foto ======

    @Test
    @DisplayName("crear() - debe crear un reporte sin foto ni clasificación IA")
    void crearReporte_sinFoto_debeFuncionar() {
        // Arrange
        Usuario usuario = crearUsuario();
        String email = usuario.getEmail();

        CrearReporteRequest request = new CrearReporteRequest();
        request.setLatitud(-33.45);
        request.setLongitud(-70.66);
        request.setDireccion("Av. Siempre Viva 742");
        request.setComuna("Santiago");
        request.setDescripcion("Humo visible en la zona");

        Reporte reporteGuardado = Reporte.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .direccion(request.getDireccion())
                .comuna(request.getComuna())
                .descripcion(request.getDescripcion())
                .estado(EstadoReporte.PENDIENTE)
                .reportadoPor(usuario)
                .build();

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteGuardado);

        // Act
        ReporteResponse response = reporteService.crear(request, null, email);

        // Assert
        assertNotNull(response);
        assertEquals("PENDIENTE", response.getEstado());
        assertEquals(-33.45, response.getLatitud());
        assertEquals("Av. Siempre Viva 742", response.getDireccion());
        assertNull(response.getFotoUrl());
        assertNull(response.getNivelGravedad());
        assertEquals("Juan Pérez", response.getReportadoPorNombre());

        verify(storageService, never()).subirFoto(any());
        verify(iaService, never()).clasificarImagen(any());
        verify(webSocketService).emitirNuevoReporte(any(ReporteResponse.class));
        verify(webSocketService, never()).emitirAlertaCritica(any());
    }

    // ====== Test 2: obtenerPorId - reporte no encontrado ======

    @Test
    @DisplayName("obtenerPorId() - debe lanzar RuntimeException si el reporte no existe")
    void obtenerPorId_noExiste_debeLanzarExcepcion() {
        // Arrange
        UUID idInexistente = UUID.randomUUID();
        when(reporteRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reporteService.obtenerPorId(idInexistente));

        assertEquals("Reporte no encontrado", ex.getMessage());
        verify(reporteRepository).findById(idInexistente);
    }

    // ====== Test 3: obtenerPorId - reporte encontrado ======

    @Test
    @DisplayName("obtenerPorId() - debe retornar el reporte correctamente mapeado a Response")
    void obtenerPorId_existe_debeRetornarResponse() {
        // Arrange
        Usuario usuario = crearUsuario();
        Reporte reporte = crearReporte(usuario);

        when(reporteRepository.findById(reporte.getId())).thenReturn(Optional.of(reporte));

        // Act
        ReporteResponse response = reporteService.obtenerPorId(reporte.getId());

        // Assert
        assertNotNull(response);
        assertEquals(reporte.getId().toString(), response.getId());
        assertEquals(reporte.getLatitud(), response.getLatitud());
        assertEquals(reporte.getLongitud(), response.getLongitud());
        assertEquals(reporte.getDireccion(), response.getDireccion());
        assertEquals(reporte.getComuna(), response.getComuna());
        assertEquals(reporte.getDescripcion(), response.getDescripcion());
        assertEquals(reporte.getFotoUrl(), response.getFotoUrl());
        assertEquals(reporte.getNivelGravedad(), response.getNivelGravedad());
        assertEquals(reporte.getConfianzaIa(), response.getConfianzaIa());
        assertEquals("CNN", response.getMetodoClasificacion());
        assertEquals("PENDIENTE", response.getEstado());
        assertEquals("Juan Pérez", response.getReportadoPorNombre());
    }

    // ====== Test 4: listar con filtro de estado ======

    @Test
    @DisplayName("listar() - debe filtrar reportes por estado cuando se provee un estado")
    void listar_conEstado_debeUsarFiltro() {
        // Arrange
        Usuario usuario = crearUsuario();
        Reporte r1 = crearReporte(usuario);
        Reporte r2 = crearReporte(usuario);
        r2.setDescripcion("Segundo incendio");

        List<Reporte> reportes = List.of(r1, r2);
        when(reporteRepository.findByEstadoOrderByCreatedAtDesc(EstadoReporte.PENDIENTE))
                .thenReturn(reportes);

        // Act
        List<ReporteResponse> resultado = reporteService.listar("PENDIENTE");

        // Assert
        assertEquals(2, resultado.size());
        verify(reporteRepository).findByEstadoOrderByCreatedAtDesc(EstadoReporte.PENDIENTE);
        verify(reporteRepository, never()).findActivos();
    }

    // ====== Test 5: actualizarEstado ======

    @Test
    @DisplayName("actualizarEstado() - debe cambiar el estado y emitir evento WebSocket")
    void actualizarEstado_debeActualizarYEmitir() {
        // Arrange
        Usuario usuario = crearUsuario();
        Reporte reporte = crearReporte(usuario);
        UUID reporteId = reporte.getId();

        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setEstado(EstadoReporte.EN_ATENCION);

        Reporte reporteActualizado = crearReporte(usuario);
        reporteActualizado.setId(reporteId);
        reporteActualizado.setEstado(EstadoReporte.EN_ATENCION);

        when(reporteRepository.findById(reporteId)).thenReturn(Optional.of(reporte));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteActualizado);

        // Act
        ReporteResponse response = reporteService.actualizarEstado(reporteId, request);

        // Assert
        assertNotNull(response);
        assertEquals("EN_ATENCION", response.getEstado());

        verify(reporteRepository).findById(reporteId);
        verify(reporteRepository).save(any(Reporte.class));
        verify(webSocketService).emitirCambioEstado(reporteId.toString(), "EN_ATENCION");
    }
}
