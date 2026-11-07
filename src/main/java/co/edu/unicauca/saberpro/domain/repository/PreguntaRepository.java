package co.edu.unicauca.saberpro.domain.repository;

import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio del agregado Pregunta, expresado en el lenguaje del dominio.
 *
 * <p>Es una interfaz pura: el dominio declara qué necesita y la capa de
 * infraestructura (JPA, JDBC, un archivo, memoria...) la implementa fuera de
 * este módulo. Se guarda y se recupera el agregado completo, nunca una opción
 * de respuesta suelta.</p>
 */
public interface PreguntaRepository {

    /** Guarda la pregunta; si no tiene identidad, el repositorio se la asigna. */
    Pregunta guardar(Pregunta pregunta);

    Optional<Pregunta> buscarPorId(Long id);

    List<Pregunta> buscarPorEstado(EstadoPregunta estado);

    void eliminar(Long id);
}
