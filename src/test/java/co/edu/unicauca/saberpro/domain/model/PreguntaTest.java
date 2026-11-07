package co.edu.unicauca.saberpro.domain.model;

import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.APROBADA;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.ARCHIVADA;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.BORRADOR;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.EN_REVISION;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.PENDIENTE_REVISION;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.PUBLICADA;
import static co.edu.unicauca.saberpro.domain.model.EstadoPregunta.RECHAZADA;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.exception.DomainException;
import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Agregado Pregunta (raíz del agregado)")
class PreguntaTest {

    private static final String MSG_CUATRO_OPCIONES = "Debe tener exactamente 4 opciones de respuesta.";
    private static final String MSG_UNA_CLAVE = "Debe existir exactamente 1 opción correcta.";

    private static Enunciado enunciado() {
        return new Enunciado("Un resistor de 6 ohmios se conecta a una fuente de 12 V. ¿Qué corriente circula?");
    }

    private static Justificacion justificacion() {
        return new Justificacion("Por la ley de Ohm, I = V / R = 12 V / 6 Ω = 2 A.");
    }

    /** Pregunta en BORRADOR con enunciado y justificación, pero sin opciones. */
    private static Pregunta preguntaSinOpciones() {
        return new Pregunta(enunciado(), justificacion());
    }

    /** Pregunta lista para revisión: 4 opciones y exactamente 1 clave (la posición 1). */
    private static Pregunta preguntaCompleta() {
        Pregunta pregunta = preguntaSinOpciones();
        pregunta.agregarOpcion("0,5 A", false);
        pregunta.agregarOpcion("2 A", true);
        pregunta.agregarOpcion("6 A", false);
        pregunta.agregarOpcion("72 A", false);
        return pregunta;
    }

    @Test
    @DisplayName("Una pregunta nueva inicia en BORRADOR, sin opciones y sin identidad")
    void preguntaNuevaIniciaEnBorrador() {
        Pregunta pregunta = new Pregunta();
        assertAll(
                () -> assertEquals(BORRADOR, pregunta.getEstado()),
                () -> assertTrue(pregunta.getOpciones().isEmpty()),
                () -> assertNull(pregunta.getId()),
                () -> assertTrue(pregunta.getClave().isEmpty()));
    }

    // ==================================================================
    @Nested
    @DisplayName("Invariantes para enviar a revisión")
    class EnvioARevision {

        @Test
        @DisplayName("No permite enviar a revisión sin cuatro opciones (caso de la guía)")
        void noDebePermitirEnviarARevisionSinCuatroOpciones() {
            Pregunta pregunta = new Pregunta();
            pregunta.agregarOpcion("Opción A", true);
            assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);
        }

        @ParameterizedTest(name = "con {0} opciones se rechaza")
        @ValueSource(ints = {0, 1, 2, 3})
        @DisplayName("No permite enviar con menos de cuatro opciones")
        void noPermiteMenosDeCuatroOpciones(int cantidad) {
            Pregunta pregunta = preguntaSinOpciones();
            for (int i = 0; i < cantidad; i++) {
                pregunta.agregarOpcion("Opción " + i, i == 0);
            }
            ReglaNegocioException ex =
                    assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);
            assertEquals(MSG_CUATRO_OPCIONES, ex.getMessage());
            assertEquals(BORRADOR, pregunta.getEstado(),
                    "si falla, el estado no cambia");
        }

        @Test
        @DisplayName("No permite enviar si ninguna opción es la clave")
        void noPermiteEnviarSinClave() {
            Pregunta pregunta = preguntaSinOpciones();
            pregunta.agregarOpcion("A", false);
            pregunta.agregarOpcion("B", false);
            pregunta.agregarOpcion("C", false);
            pregunta.agregarOpcion("D", false);
            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);
            assertEquals(MSG_UNA_CLAVE, ex.getMessage());
        }

        @Test
        @DisplayName("No permite enviar con dos claves")
        void noPermiteEnviarConDosClaves() {
            Pregunta pregunta = preguntaSinOpciones();
            pregunta.agregarOpcion("A", true);
            pregunta.agregarOpcion("B", true);
            pregunta.agregarOpcion("C", false);
            pregunta.agregarOpcion("D", false);
            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);
            assertEquals(MSG_UNA_CLAVE, ex.getMessage());
        }

        @Test
        @DisplayName("No permite enviar sin enunciado")
        void noPermiteEnviarSinEnunciado() {
            Pregunta pregunta = new Pregunta();
            pregunta.redactarJustificacion(justificacion());
            agregarCuatroOpcionesValidas(pregunta);
            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);
            assertEquals("La pregunta debe tener un enunciado.", ex.getMessage());
        }

        @Test
        @DisplayName("No permite enviar sin justificación")
        void noPermiteEnviarSinJustificacion() {
            Pregunta pregunta = new Pregunta();
            pregunta.redactarEnunciado(enunciado());
            agregarCuatroOpcionesValidas(pregunta);
            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);
            assertEquals("La pregunta debe tener una justificación.", ex.getMessage());
        }

        @Test
        @DisplayName("Con 4 opciones y 1 clave pasa a PENDIENTE_REVISION")
        void preguntaCompletaPasaAPendienteRevision() {
            Pregunta pregunta = preguntaCompleta();
            pregunta.enviarARevision();
            assertEquals(PENDIENTE_REVISION, pregunta.getEstado());
        }

        @Test
        @DisplayName("ReglaNegocioException es una excepción de dominio")
        void laExcepcionEsDeDominio() {
            Pregunta pregunta = new Pregunta();
            DomainException ex = assertThrows(DomainException.class, pregunta::enviarARevision);
            assertInstanceOf(ReglaNegocioException.class, ex);
        }

        private void agregarCuatroOpcionesValidas(Pregunta pregunta) {
            pregunta.agregarOpcion("A", true);
            pregunta.agregarOpcion("B", false);
            pregunta.agregarOpcion("C", false);
            pregunta.agregarOpcion("D", false);
        }
    }

    // ==================================================================
    @Nested
    @DisplayName("Reglas de autoría en BORRADOR")
    class Autoria {

        @Test
        @DisplayName("No permite más de cuatro opciones")
        void noPermiteMasDeCuatroOpciones() {
            Pregunta pregunta = preguntaCompleta();
            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                    () -> pregunta.agregarOpcion("144 A", false));
            assertEquals("Una pregunta no puede tener más de 4 opciones.", ex.getMessage());
            assertEquals(4, pregunta.getOpciones().size());
        }

        @Test
        @DisplayName("No permite dos opciones con el mismo texto (sin importar mayúsculas)")
        void noPermiteOpcionesRepetidas() {
            Pregunta pregunta = preguntaSinOpciones();
            pregunta.agregarOpcion("Ley de Ohm", true);
            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                    () -> pregunta.agregarOpcion("LEY DE OHM", false));
            assertEquals("Ya existe una opción con el texto \"LEY DE OHM\".", ex.getMessage());
            assertEquals(1, pregunta.getOpciones().size());
        }

        @Test
        @DisplayName("No permite una opción sin texto")
        void noPermiteOpcionVacia() {
            Pregunta pregunta = preguntaSinOpciones();
            assertThrows(ReglaNegocioException.class, () -> pregunta.agregarOpcion("  ", false));
            assertTrue(pregunta.getOpciones().isEmpty());
        }

        @Test
        @DisplayName("No permite enunciado ni justificación nulos")
        void noPermiteEnunciadoNiJustificacionNulos() {
            Pregunta pregunta = new Pregunta();
            assertThrows(ReglaNegocioException.class, () -> pregunta.redactarEnunciado(null));
            assertThrows(ReglaNegocioException.class, () -> pregunta.redactarJustificacion(null));
            assertThrows(ReglaNegocioException.class, () -> new Pregunta(null, justificacion()));
            assertThrows(ReglaNegocioException.class, () -> new Pregunta(enunciado(), null));
        }

        @Test
        @DisplayName("definirClave deja exactamente una clave en la posición elegida")
        void definirClaveDejaUnaSolaClave() {
            Pregunta pregunta = preguntaSinOpciones();
            pregunta.agregarOpcion("A", true);
            pregunta.agregarOpcion("B", true);
            pregunta.agregarOpcion("C", false);
            pregunta.agregarOpcion("D", false);

            pregunta.definirClave(2);

            List<OpcionRespuesta> opciones = pregunta.getOpciones();
            assertEquals(1, opciones.stream().filter(OpcionRespuesta::isEsCorrecta).count());
            assertTrue(opciones.get(2).isEsCorrecta());
            assertEquals("C", pregunta.getClave().orElseThrow().getTexto());
            pregunta.enviarARevision();
            assertEquals(PENDIENTE_REVISION, pregunta.getEstado());
        }

        @Test
        @DisplayName("eliminarOpcion quita la opción y permite agregar otra")
        void eliminarOpcion() {
            Pregunta pregunta = preguntaCompleta();
            pregunta.eliminarOpcion(3);
            assertEquals(3, pregunta.getOpciones().size());
            pregunta.agregarOpcion("3 A", false);
            assertEquals("3 A", pregunta.getOpciones().get(3).getTexto());
        }

        @ParameterizedTest(name = "posición {0}")
        @ValueSource(ints = {-1, 4, 10})
        @DisplayName("Rechaza posiciones que no existen")
        void rechazaPosicionesInvalidas(int posicion) {
            Pregunta pregunta = preguntaCompleta();
            assertThrows(ReglaNegocioException.class, () -> pregunta.eliminarOpcion(posicion));
            assertThrows(ReglaNegocioException.class, () -> pregunta.definirClave(posicion));
        }
    }

    // ==================================================================
    @Nested
    @DisplayName("Protección del contenido fuera de BORRADOR")
    class ContenidoBloqueado {

        @Test
        @DisplayName("Una pregunta enviada a revisión no se puede modificar")
        void noSePuedeModificarDespuesDeEnviar() {
            Pregunta pregunta = preguntaCompleta();
            pregunta.enviarARevision();
            List<OpcionRespuesta> antes = pregunta.getOpciones();

            List<Executable> modificaciones = List.of(
                    () -> pregunta.agregarOpcion("Otra", false),
                    () -> pregunta.eliminarOpcion(0),
                    () -> pregunta.definirClave(0),
                    () -> pregunta.redactarEnunciado(new Enunciado("Otro enunciado")),
                    () -> pregunta.redactarJustificacion(new Justificacion("Otra justificación válida.")));

            for (Executable modificacion : modificaciones) {
                ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, modificacion);
                assertTrue(ex.getMessage().startsWith("Solo se puede modificar una pregunta en estado BORRADOR"));
            }
            assertEquals(antes, pregunta.getOpciones());
            assertEquals(enunciado(), pregunta.getEnunciado());
        }

        @Test
        @DisplayName("La lista de opciones que se entrega es de solo lectura")
        void opcionesSonDeSoloLectura() {
            Pregunta pregunta = preguntaSinOpciones();
            List<OpcionRespuesta> opciones = pregunta.getOpciones();
            assertThrows(UnsupportedOperationException.class,
                    () -> opciones.add(new OpcionRespuesta("Intrusa", true)));
            assertTrue(pregunta.getOpciones().isEmpty());
        }
    }

    // ==================================================================
    @Nested
    @DisplayName("Ciclo de vida")
    class CicloDeVida {

        @Test
        @DisplayName("Flujo completo: BORRADOR → PENDIENTE_REVISION → EN_REVISION → APROBADA")
        void flujoHastaAprobada() {
            Pregunta pregunta = preguntaCompleta();
            pregunta.enviarARevision();
            pregunta.iniciarRevision();
            assertEquals(EN_REVISION, pregunta.getEstado());
            pregunta.aprobar();
            assertEquals(APROBADA, pregunta.getEstado());
        }

        @Test
        @DisplayName("Una pregunta rechazada vuelve a BORRADOR, se corrige y se reenvía")
        void rechazadaSeCorrigeYSeReenvia() {
            Pregunta pregunta = preguntaCompleta();
            pregunta.enviarARevision();
            pregunta.iniciarRevision();
            pregunta.rechazar();
            assertEquals(RECHAZADA, pregunta.getEstado());

            pregunta.devolverABorrador();
            pregunta.redactarJustificacion(new Justificacion("I = V / R = 12 / 6 = 2 A; las demás opciones confunden V y R."));
            pregunta.enviarARevision();

            assertEquals(PENDIENTE_REVISION, pregunta.getEstado());
        }

        @Test
        @DisplayName("Una pregunta pendiente se puede retirar a BORRADOR")
        void pendienteSePuedeRetirar() {
            Pregunta pregunta = preguntaCompleta();
            pregunta.enviarARevision();
            pregunta.devolverABorrador();
            assertEquals(BORRADOR, pregunta.getEstado());
        }

        @Test
        @DisplayName("No se puede aprobar ni rechazar una pregunta que no está en revisión")
        void noSeApruebaSinRevision() {
            Pregunta pregunta = preguntaCompleta();
            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, pregunta::aprobar);
            assertEquals("No se permite pasar una pregunta de BORRADOR a APROBADA.", ex.getMessage());
            assertThrows(ReglaNegocioException.class, pregunta::rechazar);
            assertThrows(ReglaNegocioException.class, pregunta::iniciarRevision);
            assertEquals(BORRADOR, pregunta.getEstado());
        }

        @Test
        @DisplayName("No se puede enviar dos veces a revisión")
        void noSeEnviaDosVeces() {
            Pregunta pregunta = preguntaCompleta();
            pregunta.enviarARevision();
            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, pregunta::enviarARevision);
            assertEquals("No se permite pasar una pregunta de PENDIENTE_REVISION a PENDIENTE_REVISION.",
                    ex.getMessage());
        }

        @Test
        @DisplayName("Una pregunta aprobada se publica y luego se archiva; ARCHIVADA es final")
        void aprobadaSePublicaYSeArchiva() {
            Pregunta pregunta = preguntaCompleta();
            pregunta.enviarARevision();
            pregunta.iniciarRevision();
            pregunta.aprobar();
            assertThrows(ReglaNegocioException.class, pregunta::devolverABorrador);
            assertThrows(ReglaNegocioException.class, pregunta::archivar);

            pregunta.publicar();
            assertEquals(PUBLICADA, pregunta.getEstado());
            pregunta.archivar();
            assertEquals(ARCHIVADA, pregunta.getEstado());

            assertThrows(ReglaNegocioException.class, pregunta::publicar);
            assertThrows(ReglaNegocioException.class, pregunta::devolverABorrador);
            assertEquals(ARCHIVADA, pregunta.getEstado());
        }

        @Test
        @DisplayName("No se puede publicar una pregunta que no fue aprobada")
        void noSePublicaSinAprobacion() {
            Pregunta pregunta = preguntaCompleta();
            assertThrows(ReglaNegocioException.class, pregunta::publicar);
            pregunta.enviarARevision();
            pregunta.iniciarRevision();
            assertThrows(ReglaNegocioException.class, pregunta::publicar);
            pregunta.rechazar();
            assertThrows(ReglaNegocioException.class, pregunta::publicar);
            assertEquals(RECHAZADA, pregunta.getEstado());
        }
    }

    // ==================================================================
    @Nested
    @DisplayName("Identidad de la entidad raíz")
    class Identidad {

        @Test
        @DisplayName("Dos preguntas con el mismo id son la misma, aunque cambie su contenido")
        void igualdadPorIdentidad() {
            Pregunta a = preguntaCompleta();
            a.asignarId(7L);
            Pregunta b = new Pregunta();
            b.asignarId(7L);
            Pregunta c = preguntaCompleta();
            c.asignarId(8L);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, c);
        }

        @Test
        @DisplayName("Sin id, una pregunta solo es igual a sí misma")
        void sinIdSoloEsIgualASiMisma() {
            Pregunta a = preguntaCompleta();
            Pregunta b = preguntaCompleta();
            assertEquals(a, a);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("La identidad no puede cambiar una vez asignada")
        void identidadNoCambia() {
            Pregunta pregunta = new Pregunta();
            pregunta.asignarId(1L);
            pregunta.asignarId(1L);
            assertThrows(ReglaNegocioException.class, () -> pregunta.asignarId(2L));
            assertEquals(1L, pregunta.getId());
        }

        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {0L, -5L})
        @DisplayName("La identidad debe ser un número positivo")
        void identidadPositiva(long id) {
            Pregunta pregunta = new Pregunta();
            assertThrows(ReglaNegocioException.class, () -> pregunta.asignarId(id));
            assertThrows(ReglaNegocioException.class, () -> pregunta.asignarId(null));
        }
    }

    // ==================================================================
    @Nested
    @DisplayName("Reconstitución desde un repositorio")
    class Reconstitucion {

        private final List<OpcionRespuesta> cuatroOpciones = List.of(
                new OpcionRespuesta("0,5 A", false),
                new OpcionRespuesta("2 A", true),
                new OpcionRespuesta("6 A", false),
                new OpcionRespuesta("72 A", false));

        @Test
        @DisplayName("Reconstruye una pregunta válida con su estado")
        void reconstruyePreguntaValida() {
            Pregunta pregunta = Pregunta.reconstituir(10L, enunciado(), justificacion(),
                    cuatroOpciones, EN_REVISION);
            assertAll(
                    () -> assertEquals(10L, pregunta.getId()),
                    () -> assertEquals(EN_REVISION, pregunta.getEstado()),
                    () -> assertEquals(cuatroOpciones, pregunta.getOpciones()),
                    () -> assertEquals("2 A", pregunta.getClave().orElseThrow().getTexto()));
        }

        @Test
        @DisplayName("Un BORRADOR incompleto se puede reconstruir")
        void borradorIncompleto() {
            Pregunta pregunta = Pregunta.reconstituir(11L, null, null, List.of(), BORRADOR);
            assertEquals(BORRADOR, pregunta.getEstado());
            assertTrue(pregunta.getOpciones().isEmpty());
            assertFalse(pregunta.getClave().isPresent());
        }

        @Test
        @DisplayName("No reconstruye una pregunta aprobada que incumple las invariantes")
        void noReconstruyeAprobadaInvalida() {
            List<OpcionRespuesta> sinClave = List.of(
                    new OpcionRespuesta("A", false), new OpcionRespuesta("B", false),
                    new OpcionRespuesta("C", false), new OpcionRespuesta("D", false));
            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                    () -> Pregunta.reconstituir(12L, enunciado(), justificacion(), sinClave, APROBADA));
            assertEquals(MSG_UNA_CLAVE, ex.getMessage());
        }

        @Test
        @DisplayName("Exige identidad, estado y lista de opciones")
        void exigeDatosObligatorios() {
            assertThrows(ReglaNegocioException.class,
                    () -> Pregunta.reconstituir(null, enunciado(), justificacion(), cuatroOpciones, BORRADOR));
            assertThrows(ReglaNegocioException.class,
                    () -> Pregunta.reconstituir(1L, enunciado(), justificacion(), cuatroOpciones, null));
            assertThrows(ReglaNegocioException.class,
                    () -> Pregunta.reconstituir(1L, enunciado(), justificacion(), null, BORRADOR));
        }
    }
}
