package co.edu.unicauca.saberpro.domain.repository;

import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementación en memoria del repositorio, solo para pruebas. Demuestra que
 * la interfaz del dominio se puede implementar sin ningún framework.
 */
class PreguntaRepositoryEnMemoria implements PreguntaRepository {

    private final Map<Long, Pregunta> almacen = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    @Override
    public Pregunta guardar(Pregunta pregunta) {
        if (pregunta.getId() == null) {
            pregunta.asignarId(secuencia.incrementAndGet());
        }
        almacen.put(pregunta.getId(), pregunta);
        return pregunta;
    }

    @Override
    public Optional<Pregunta> buscarPorId(Long id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public List<Pregunta> buscarPorEstado(EstadoPregunta estado) {
        return almacen.values().stream()
                .filter(pregunta -> pregunta.getEstado() == estado)
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        almacen.remove(id);
    }
}
