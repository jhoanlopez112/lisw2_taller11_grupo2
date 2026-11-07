package co.edu.unicauca.saberpro.domain.model;

/**
 * Estados del ciclo de vida de una pregunta y las transiciones permitidas.
 *
 * <ul>
 *   <li>BORRADOR → PENDIENTE_REVISION ({@code enviarARevision})</li>
 *   <li>PENDIENTE_REVISION → EN_REVISION ({@code iniciarRevision})
 *       o BORRADOR ({@code devolverABorrador})</li>
 *   <li>EN_REVISION → APROBADA ({@code aprobar}) o RECHAZADA ({@code rechazar})</li>
 *   <li>RECHAZADA → BORRADOR ({@code devolverABorrador})</li>
 *   <li>APROBADA → PUBLICADA ({@code publicar})</li>
 *   <li>PUBLICADA → ARCHIVADA ({@code archivar})</li>
 *   <li>ARCHIVADA es un estado final.</li>
 * </ul>
 */
public enum EstadoPregunta {

    BORRADOR,
    PENDIENTE_REVISION,
    EN_REVISION,
    APROBADA,
    RECHAZADA,
    PUBLICADA,
    ARCHIVADA;

    /** Indica si desde este estado se puede pasar al estado destino. */
    public boolean puedeTransitarA(EstadoPregunta destino) {
        if (destino == null) {
            return false;
        }
        return switch (this) {
            case BORRADOR -> destino == PENDIENTE_REVISION;
            case PENDIENTE_REVISION ->
                    destino == EN_REVISION || destino == BORRADOR;
            case EN_REVISION -> destino == APROBADA || destino == RECHAZADA;
            case RECHAZADA -> destino == BORRADOR;
            case APROBADA -> destino == PUBLICADA;
            case PUBLICADA -> destino == ARCHIVADA;
            case ARCHIVADA -> false;
        };
    }

    /** Solo una pregunta en BORRADOR puede cambiar su contenido. */
    public boolean esEditable() {
        return this == BORRADOR;
    }
}
