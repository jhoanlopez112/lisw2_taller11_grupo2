package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.util.Objects;

/**
 * Objeto de valor (Value Object) inmutable que representa una opción de
 * respuesta: su texto y el indicador de si es la clave (la opción correcta)
 * o un distractor.
 *
 * <p>Como es inmutable, "cambiar" el indicador de clave significa crear una
 * nueva opción con {@link #conIndicadorDeClave(boolean)}.</p>
 */
public final class OpcionRespuesta {

    private final String texto;
    private final boolean esCorrecta;

    public OpcionRespuesta(String texto, boolean esCorrecta) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El texto de la opción de respuesta no puede estar vacío.");
        }
        this.texto = texto.trim();
        this.esCorrecta = esCorrecta;
    }

    public String getTexto() {
        return texto;
    }

    public boolean isEsCorrecta() {
        return esCorrecta;
    }

    /**
     * Devuelve una opción con el mismo texto y el indicador de clave pedido.
     * La opción original no cambia.
     */
    public OpcionRespuesta conIndicadorDeClave(boolean nuevaEsCorrecta) {
        if (nuevaEsCorrecta == esCorrecta) {
            return this;
        }
        return new OpcionRespuesta(texto, nuevaEsCorrecta);
    }

    /** Indica si dos opciones dicen lo mismo, sin importar mayúsculas. */
    public boolean tieneMismoTextoQue(OpcionRespuesta otra) {
        return otra != null && texto.equalsIgnoreCase(otra.texto);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpcionRespuesta otra)) {
            return false;
        }
        return esCorrecta == otra.esCorrecta && texto.equals(otra.texto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texto, esCorrecta);
    }

    @Override
    public String toString() {
        return (esCorrecta ? "[clave] " : "[distractor] ") + texto;
    }
}
