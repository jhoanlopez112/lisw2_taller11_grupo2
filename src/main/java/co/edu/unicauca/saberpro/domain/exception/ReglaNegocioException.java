package co.edu.unicauca.saberpro.domain.exception;

/**
 * Se lanza cuando una operación incumple una invariante o regla del negocio
 * del Banco de Preguntas Saber PRO (por ejemplo, una justificación demasiado
 * corta o una pregunta que no tiene exactamente una clave).
 */
public class ReglaNegocioException extends DomainException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
