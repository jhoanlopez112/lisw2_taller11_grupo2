package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Objeto de valor (Value Object) inmutable con la justificación pedagógica de
 * una pregunta: explica por qué la clave es correcta.
 *
 * <p>Reglas del negocio:</p>
 * <ul>
 *   <li>Debe tener al menos {@value #LONGITUD_MINIMA} caracteres, sin contar
 *       los espacios al inicio y al final.</li>
 *   <li>No puede contener expresiones prohibidas: frases que reemplazan el
 *       argumento por una apelación a lo "obvio" o texto de relleno. La
 *       comparación ignora mayúsculas y tildes, y busca palabras completas
 *       (por ejemplo, "obviar" no coincide con "obviamente").</li>
 * </ul>
 */
public final class Justificacion {

    public static final int LONGITUD_MINIMA = 15;

    /** Expresiones prohibidas, ya normalizadas (minúsculas y sin tildes). */
    public static final Set<String> EXPRESIONES_PROHIBIDAS = Set.of(
            "obviamente",
            "evidentemente",
            "es obvio",
            "porque si",
            "lorem ipsum");

    private final String texto;

    public Justificacion(String texto) {
        if (texto == null || texto.trim().length() < LONGITUD_MINIMA) {
            throw new ReglaNegocioException("La justificación debe contener al menos "
                    + LONGITUD_MINIMA + " caracteres.");
        }
        String limpio = texto.trim();
        Optional<String> prohibida = buscarExpresionProhibida(limpio);
        if (prohibida.isPresent()) {
            throw new ReglaNegocioException(
                    "La justificación contiene la expresión prohibida \""
                    + prohibida.get()
                    + "\". Explique por qué la clave es correcta.");
        }
        this.texto = limpio;
    }

    public String getTexto() {
        return texto;
    }

    private static Optional<String> buscarExpresionProhibida(String texto) {
        String normalizado = normalizar(texto);
        return EXPRESIONES_PROHIBIDAS.stream()
                .filter(expresion -> normalizado.contains(" " + expresion + " "))
                .sorted()
                .findFirst();
    }

    /**
     * Pasa el texto a minúsculas, quita las tildes y reemplaza todo lo que no
     * sea letra o número por un espacio. Se rodea con espacios para poder
     * buscar palabras completas.
     */
    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String soloPalabras = sinTildes.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        return " " + soloPalabras + " ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Justificacion otra)) {
            return false;
        }
        return texto.equals(otra.texto);
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
