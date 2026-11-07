package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;

/**
 * Objeto de valor (Value Object) inmutable que representa el enunciado de una
 * pregunta: la situación o pregunta directa que lee el estudiante.
 *
 * <p>No tiene identidad propia: dos enunciados con el mismo texto son el mismo
 * enunciado. Su única regla es que no puede ser nulo ni estar vacío.</p>
 */
public final class Enunciado {

    private final String texto;

    public Enunciado(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException(
                    "El enunciado de la pregunta no puede ser nulo ni estar vacío.");
        }
        this.texto = texto.trim();
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Enunciado otro)) {
            return false;
        }
        return texto.equals(otro.texto);
    }

    @Override
    public int hashCode() {
        return texto.hashCode();
    }

    @Override
    public String toString() {
        return texto;
    }
}
