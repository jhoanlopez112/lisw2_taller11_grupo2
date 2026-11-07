package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Objeto de valor Enunciado")
class EnunciadoTest {

    @ParameterizedTest(name = "rechaza el texto [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    @DisplayName("No permite un enunciado nulo o vacío")
    void noPermiteEnunciadoNuloOVacio(String texto) {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> new Enunciado(texto));
        assertEquals("El enunciado de la pregunta no puede ser nulo ni estar vacío.", ex.getMessage());
    }

    @Test
    @DisplayName("Guarda el texto sin espacios al inicio ni al final")
    void guardaElTextoRecortado() {
        Enunciado enunciado = new Enunciado("  ¿Cuál es la capital de Colombia?  ");
        assertEquals("¿Cuál es la capital de Colombia?", enunciado.getTexto());
    }

    @Test
    @DisplayName("Dos enunciados con el mismo texto son iguales (igualdad por valor)")
    void igualdadPorValor() {
        Enunciado a = new Enunciado("¿Cuánto es 2 + 2?");
        Enunciado b = new Enunciado(" ¿Cuánto es 2 + 2? ");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Enunciado("¿Cuánto es 2 + 3?"));
    }
}
