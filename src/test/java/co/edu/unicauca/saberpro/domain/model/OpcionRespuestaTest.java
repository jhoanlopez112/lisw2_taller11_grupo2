package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Objeto de valor OpcionRespuesta")
class OpcionRespuestaTest {

    @ParameterizedTest(name = "rechaza el texto [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("No permite una opción sin texto")
    void noPermiteOpcionSinTexto(String texto) {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> new OpcionRespuesta(texto, false));
        assertEquals("El texto de la opción de respuesta no puede estar vacío.", ex.getMessage());
    }

    @Test
    @DisplayName("Guarda el texto recortado y el indicador de clave")
    void guardaTextoEIndicador() {
        OpcionRespuesta opcion = new OpcionRespuesta("  2 A ", true);
        assertEquals("2 A", opcion.getTexto());
        assertTrue(opcion.isEsCorrecta());
    }

    @Test
    @DisplayName("Es inmutable: cambiar el indicador de clave crea una opción nueva")
    void cambiarIndicadorCreaOtraOpcion() {
        OpcionRespuesta distractor = new OpcionRespuesta("4 A", false);

        OpcionRespuesta clave = distractor.conIndicadorDeClave(true);

        assertTrue(clave.isEsCorrecta());
        assertFalse(distractor.isEsCorrecta(), "la opción original no debe cambiar");
        assertEquals(distractor.getTexto(), clave.getTexto());
        assertSame(distractor, distractor.conIndicadorDeClave(false), "sin cambios devuelve la misma opción");
    }

    @Test
    @DisplayName("La igualdad por valor considera el texto y el indicador de clave")
    void igualdadPorValor() {
        OpcionRespuesta a = new OpcionRespuesta("2 A", true);
        assertEquals(a, new OpcionRespuesta("2 A", true));
        assertEquals(a.hashCode(), new OpcionRespuesta("2 A", true).hashCode());
        assertNotEquals(a, new OpcionRespuesta("2 A", false));
        assertNotEquals(a, new OpcionRespuesta("4 A", true));
    }

    @Test
    @DisplayName("Detecta textos repetidos sin importar mayúsculas")
    void detectaTextoRepetido() {
        OpcionRespuesta a = new OpcionRespuesta("Ley de Ohm", false);
        assertTrue(a.tieneMismoTextoQue(new OpcionRespuesta("LEY DE OHM", true)));
        assertFalse(a.tieneMismoTextoQue(new OpcionRespuesta("Ley de Kirchhoff", false)));
        assertFalse(a.tieneMismoTextoQue(null));
    }
}
