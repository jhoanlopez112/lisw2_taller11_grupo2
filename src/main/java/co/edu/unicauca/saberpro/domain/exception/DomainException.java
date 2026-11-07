package co.edu.unicauca.saberpro.domain.exception;

/**
 * Excepción base de la capa de dominio.
 *
 * <p>Toda violación de una regla del negocio se comunica con una subclase de
 * esta excepción. Es una excepción no verificada (unchecked) para que el
 * modelo exprese sus reglas sin obligar a los clientes a capturarla, y no
 * depende de ningún framework.</p>
 */
public abstract class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected DomainException(String mensaje) {
        super(mensaje);
    }
}
