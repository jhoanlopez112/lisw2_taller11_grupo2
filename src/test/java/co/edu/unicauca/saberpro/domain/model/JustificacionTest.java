package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Objeto de valor Justificacion")
class JustificacionTest {

    private static final String MENSAJE_LONGITUD = "La justificación debe contener al menos 15 caracteres.";

    @Test
    @DisplayName("No permite una justificación nula")
    void noPermiteJustificacionNula() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> new Justificacion(null));
        assertEquals(MENSAJE_LONGITUD, ex.getMessage());
    }

    @ParameterizedTest(name = "rechaza [{0}]")
    @ValueSource(strings = {"", "   ", "Muy corta", "14 caracteres."})
    @DisplayName("No permite justificaciones de menos de 15 caracteres")
    void noPermiteJustificacionCorta(String texto) {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> new Justificacion(texto));
        assertEquals(MENSAJE_LONGITUD, ex.getMessage());
    }

    @Test
    @DisplayName("Límite: 14 caracteres se rechaza y 15 caracteres se acepta")
    void limiteDeQuinceCaracteres() {
        assertThrows(ReglaNegocioException.class, () -> new Justificacion("x".repeat(14)));
        assertDoesNotThrow(() -> new Justificacion("x".repeat(15)));
    }

    @Test
    @DisplayName("Los espacios al inicio y al final no cuentan para el mínimo")
    void losEspaciosNoCuentan() {
        String texto = "     " + "x".repeat(14) + "     ";
        assertTrue(texto.length() > 15);
        assertThrows(ReglaNegocioException.class, () -> new Justificacion(texto));
    }

    @Test
    @DisplayName("Guarda el texto recortado")
    void guardaElTextoRecortado() {
        Justificacion justificacion = new Justificacion("  La opción B aplica la ley de Ohm.  ");
        assertEquals("La opción B aplica la ley de Ohm.", justificacion.getTexto());
    }

    @ParameterizedTest(name = "rechaza [{0}]")
    @ValueSource(strings = {
            "Obviamente la respuesta correcta es la B.",
            "EVIDENTEMENTE se aplica la ley de Ohm al circuito.",
            "La clave es la C porque sí, no hay más que decir.",
            "Es obvio que el resultado es de 2 amperios.",
            "Lorem ipsum dolor sit amet, consectetur."
    })
    @DisplayName("No permite expresiones prohibidas (sin importar mayúsculas ni tildes)")
    void noPermiteExpresionesProhibidas(String texto) {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> new Justificacion(texto));
        assertTrue(ex.getMessage().startsWith("La justificación contiene la expresión prohibida"),
                ex.getMessage());
    }

    @ParameterizedTest(name = "acepta [{0}]")
    @ValueSource(strings = {
            "Se puede obviar la resistencia interna porque es despreciable.",
            "La evidencia experimental confirma que la opción A es correcta."
    })
    @DisplayName("Solo busca palabras completas: 'obviar' o 'evidencia' sí se permiten")
    void permitePalabrasParecidasALasProhibidas(String texto) {
        assertDoesNotThrow(() -> new Justificacion(texto));
    }

    @Test
    @DisplayName("Dos justificaciones con el mismo texto son iguales (igualdad por valor)")
    void igualdadPorValor() {
        Justificacion a = new Justificacion("La opción B aplica la ley de Ohm.");
        Justificacion b = new Justificacion("La opción B aplica la ley de Ohm.");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Justificacion("La opción C aplica la ley de Kirchhoff."));
    }
}
