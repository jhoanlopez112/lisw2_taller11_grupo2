package co.edu.unicauca.saberpro.domain.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.model.Enunciado;
import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Justificacion;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Repositorio PreguntaRepository (interfaz pura)")
class PreguntaRepositoryTest {

    private final PreguntaRepository repositorio = new PreguntaRepositoryEnMemoria();

    private static Pregunta preguntaCompleta(String enunciado) {
        Pregunta pregunta = new Pregunta(new Enunciado(enunciado),
                new Justificacion("La opción A es la única que cumple la condición."));
        pregunta.agregarOpcion("A", true);
        pregunta.agregarOpcion("B", false);
        pregunta.agregarOpcion("C", false);
        pregunta.agregarOpcion("D", false);
        return pregunta;
    }

    @Test
    @DisplayName("Al guardar una pregunta nueva se le asigna identidad")
    void guardarAsignaIdentidad() {
        Pregunta guardada = repositorio.guardar(preguntaCompleta("¿Pregunta 1?"));
        assertNotNull(guardada.getId());
        assertEquals(guardada, repositorio.buscarPorId(guardada.getId()).orElseThrow());
    }

    @Test
    @DisplayName("Busca preguntas por estado y las elimina por identidad")
    void buscarPorEstadoYEliminar() {
        Pregunta borrador = repositorio.guardar(preguntaCompleta("¿Pregunta 1?"));
        Pregunta enviada = preguntaCompleta("¿Pregunta 2?");
        enviada.enviarARevision();
        repositorio.guardar(enviada);

        List<Pregunta> pendientes = repositorio.buscarPorEstado(EstadoPregunta.PENDIENTE_REVISION);
        assertEquals(List.of(enviada), pendientes);

        repositorio.eliminar(borrador.getId());
        assertTrue(repositorio.buscarPorId(borrador.getId()).isEmpty());
    }
}
