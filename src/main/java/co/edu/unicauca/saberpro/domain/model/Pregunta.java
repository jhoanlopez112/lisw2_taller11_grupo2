package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado Pregunta (Aggregate Root) del contexto de Autoría.
 *
 * <p>Es la única puerta de entrada para modificar el agregado: las opciones de
 * respuesta, el enunciado y la justificación solo cambian a través de sus
 * métodos, y cada método protege las invariantes del negocio:</p>
 * <ol>
 *   <li>El contenido solo se modifica en estado {@link EstadoPregunta#BORRADOR}.</li>
 *   <li>Una pregunta nunca tiene más de {@value #NUMERO_DE_OPCIONES} opciones
 *       ni dos opciones con el mismo texto.</li>
 *   <li>Para enviarla a revisión debe tener enunciado, justificación,
 *       exactamente {@value #NUMERO_DE_OPCIONES} opciones y exactamente
 *       {@value #NUMERO_DE_CLAVES} clave.</li>
 *   <li>Los cambios de estado siguen las transiciones de {@link EstadoPregunta}.</li>
 *   <li>La identidad, una vez asignada, no cambia.</li>
 * </ol>
 *
 * <p>Es una entidad: dos preguntas son la misma si tienen el mismo id, aunque
 * su contenido sea distinto.</p>
 */
public class Pregunta {

    public static final int NUMERO_DE_OPCIONES = 4;
    public static final int NUMERO_DE_CLAVES = 1;

    private Long id;
    private Enunciado enunciado;
    private Justificacion justificacion;
    private final List<OpcionRespuesta> opciones = new ArrayList<>();
    private EstadoPregunta estado = EstadoPregunta.BORRADOR;

    /** Crea una pregunta vacía en estado BORRADOR. */
    public Pregunta() {
    }

    /** Crea una pregunta en estado BORRADOR con su enunciado y su justificación. */
    public Pregunta(Enunciado enunciado, Justificacion justificacion) {
        this.enunciado = exigirEnunciado(enunciado);
        this.justificacion = exigirJustificacion(justificacion);
    }

    /**
     * Reconstruye una pregunta que ya existía (por ejemplo, al leerla desde un
     * repositorio). Si la pregunta no está en BORRADOR, se verifica que cumpla
     * las invariantes de envío, porque ya las tuvo que cumplir para salir de
     * ese estado.
     */
    public static Pregunta reconstituir(Long id, Enunciado enunciado, Justificacion justificacion,
                                        List<OpcionRespuesta> opciones, EstadoPregunta estado) {
        if (estado == null) {
            throw new ReglaNegocioException("El estado de la pregunta es obligatorio.");
        }
        if (opciones == null) {
            throw new ReglaNegocioException("La lista de opciones no puede ser nula.");
        }
        Pregunta pregunta = new Pregunta();
        pregunta.asignarId(id);
        pregunta.enunciado = enunciado;
        pregunta.justificacion = justificacion;
        for (OpcionRespuesta opcion : opciones) {
            pregunta.incorporarOpcion(opcion);
        }
        if (estado != EstadoPregunta.BORRADOR) {
            pregunta.validarInvariantesParaEnvio();
        }
        pregunta.estado = estado;
        return pregunta;
    }

    // ------------------------------------------------------------------
    // Comportamiento de autoría (solo en BORRADOR)
    // ------------------------------------------------------------------

    public void redactarEnunciado(Enunciado nuevoEnunciado) {
        verificarQueEsEditable();
        this.enunciado = exigirEnunciado(nuevoEnunciado);
    }

    public void redactarJustificacion(Justificacion nuevaJustificacion) {
        verificarQueEsEditable();
        this.justificacion = exigirJustificacion(nuevaJustificacion);
    }

    public void agregarOpcion(String texto, boolean esCorrecta) {
        verificarQueEsEditable();
        incorporarOpcion(new OpcionRespuesta(texto, esCorrecta));
    }

    /** Elimina la opción en la posición indicada (0 a 3). */
    public void eliminarOpcion(int posicion) {
        verificarQueEsEditable();
        verificarPosicion(posicion);
        opciones.remove(posicion);
    }

    /**
     * Marca como clave la opción en la posición indicada (0 a 3) y convierte
     * todas las demás en distractores. Así la pregunta queda con una sola clave.
     */
    public void definirClave(int posicion) {
        verificarQueEsEditable();
        verificarPosicion(posicion);
        for (int i = 0; i < opciones.size(); i++) {
            opciones.set(i, opciones.get(i).conIndicadorDeClave(i == posicion));
        }
    }

    // ------------------------------------------------------------------
    // Ciclo de vida
    // ------------------------------------------------------------------

    public void enviarARevision() {
        verificarTransicion(EstadoPregunta.PENDIENTE_REVISION);
        validarInvariantesParaEnvio();
        this.estado = EstadoPregunta.PENDIENTE_REVISION;
    }

    public void iniciarRevision() {
        transitarA(EstadoPregunta.EN_REVISION);
    }

    public void aprobar() {
        transitarA(EstadoPregunta.APROBADA);
    }

    public void rechazar() {
        transitarA(EstadoPregunta.RECHAZADA);
    }

    /** Retira una pregunta pendiente o devuelve una rechazada para corregirla. */
    public void devolverABorrador() {
        transitarA(EstadoPregunta.BORRADOR);
    }

    /** Deja una pregunta aprobada disponible para los simulacros. */
    public void publicar() {
        transitarA(EstadoPregunta.PUBLICADA);
    }

    /** Retira del uso una pregunta publicada. Es un estado final. */
    public void archivar() {
        transitarA(EstadoPregunta.ARCHIVADA);
    }

    // ------------------------------------------------------------------
    // Identidad
    // ------------------------------------------------------------------

    /** Asigna la identidad de la pregunta. Se asigna una sola vez. */
    public void asignarId(Long nuevoId) {
        if (nuevoId == null || nuevoId <= 0) {
            throw new ReglaNegocioException("La identidad de la pregunta debe ser un número positivo.");
        }
        if (this.id != null && !this.id.equals(nuevoId)) {
            throw new ReglaNegocioException("La identidad de una pregunta no puede cambiar una vez asignada.");
        }
        this.id = nuevoId;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public Enunciado getEnunciado() {
        return enunciado;
    }

    public Justificacion getJustificacion() {
        return justificacion;
    }

    /** Copia de solo lectura: nadie puede agregar opciones saltándose las reglas. */
    public List<OpcionRespuesta> getOpciones() {
        return List.copyOf(opciones);
    }

    public EstadoPregunta getEstado() {
        return estado;
    }

    /** La primera opción marcada como clave, si existe. */
    public Optional<OpcionRespuesta> getClave() {
        return opciones.stream().filter(OpcionRespuesta::isEsCorrecta).findFirst();
    }

    // ------------------------------------------------------------------
    // Reglas internas
    // ------------------------------------------------------------------

    private static Enunciado exigirEnunciado(Enunciado enunciado) {
        if (enunciado == null) {
            throw new ReglaNegocioException("El enunciado de la pregunta es obligatorio.");
        }
        return enunciado;
    }

    private static Justificacion exigirJustificacion(Justificacion justificacion) {
        if (justificacion == null) {
            throw new ReglaNegocioException("La justificación de la pregunta es obligatoria.");
        }
        return justificacion;
    }

    private void incorporarOpcion(OpcionRespuesta nueva) {
        if (nueva == null) {
            throw new ReglaNegocioException("La opción de respuesta no puede ser nula.");
        }
        if (opciones.size() >= NUMERO_DE_OPCIONES) {
            throw new ReglaNegocioException(
                    "Una pregunta no puede tener más de " + NUMERO_DE_OPCIONES + " opciones.");
        }
        boolean repetida = opciones.stream().anyMatch(opcion -> opcion.tieneMismoTextoQue(nueva));
        if (repetida) {
            throw new ReglaNegocioException(
                    "Ya existe una opción con el texto \"" + nueva.getTexto() + "\".");
        }
        opciones.add(nueva);
    }

    private void validarInvariantesParaEnvio() {
        if (enunciado == null) {
            throw new ReglaNegocioException("La pregunta debe tener un enunciado.");
        }
        if (justificacion == null) {
            throw new ReglaNegocioException(
                    "La pregunta debe tener una justificación.");
        }
        if (opciones.size() != NUMERO_DE_OPCIONES) {
            throw new ReglaNegocioException("Debe tener exactamente "
                    + NUMERO_DE_OPCIONES + " opciones de respuesta.");
        }
        long correctas = opciones.stream()
                .filter(OpcionRespuesta::isEsCorrecta).count();
        if (correctas != NUMERO_DE_CLAVES) {
            throw new ReglaNegocioException("Debe existir exactamente "
                    + NUMERO_DE_CLAVES + " opción correcta.");
        }
    }

    private void verificarQueEsEditable() {
        if (!estado.esEditable()) {
            throw new ReglaNegocioException(
                    "Solo se puede modificar una pregunta en estado BORRADOR (estado actual: " + estado + ").");
        }
    }

    private void verificarPosicion(int posicion) {
        if (posicion < 0 || posicion >= opciones.size()) {
            throw new ReglaNegocioException("No existe una opción en la posición " + posicion + ".");
        }
    }

    private void transitarA(EstadoPregunta destino) {
        verificarTransicion(destino);
        this.estado = destino;
    }

    private void verificarTransicion(EstadoPregunta destino) {
        if (!estado.puedeTransitarA(destino)) {
            throw new ReglaNegocioException(
                    "No se permite pasar una pregunta de " + estado + " a " + destino + ".");
        }
    }

    // ------------------------------------------------------------------
    // Igualdad por identidad (entidad)
    // ------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pregunta otra)) {
            return false;
        }
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Pregunta{id=" + id + ", estado=" + estado + ", opciones=" + opciones.size() + "}";
    }
}
