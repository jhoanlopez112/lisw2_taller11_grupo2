package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Ciclo de vida EstadoPregunta")
class EstadoPreguntaTest {

    /** Tabla de transiciones permitidas según el contexto de Ciclo de Vida. */
    private static final Set<String> PERMITIDAS = Set.of(
            "BORRADOR->PENDIENTE_REVISION",
            "PENDIENTE_REVISION->EN_REVISION",
            "PENDIENTE_REVISION->BORRADOR",
            "EN_REVISION->APROBADA",
            "EN_REVISION->RECHAZADA",
            "RECHAZADA->BORRADOR",
            "APROBADA->PUBLICADA",
            "PUBLICADA->ARCHIVADA");

    static Stream<Arguments> todasLasCombinaciones() {
        return Arrays.stream(EstadoPregunta.values())
                .flatMap(origen -> Arrays.stream(EstadoPregunta.values())
                        .map(destino -> Arguments.of(origen, destino,
                                PERMITIDAS.contains(origen + "->" + destino))));
    }

    @ParameterizedTest(name = "{0} -> {1}: permitida = {2}")
    @MethodSource("todasLasCombinaciones")
    @DisplayName("Solo se permiten las transiciones de la tabla")
    void transiciones(EstadoPregunta origen, EstadoPregunta destino, boolean permitida) {
        assertEquals(permitida, origen.puedeTransitarA(destino));
    }

    @ParameterizedTest(name = "{0} -> null")
    @EnumSource(EstadoPregunta.class)
    @DisplayName("Ningún estado puede pasar a un destino nulo")
    void destinoNuloNoPermitido(EstadoPregunta origen) {
        assertFalse(origen.puedeTransitarA(null));
    }

    @Test
    @DisplayName("Solo el estado BORRADOR es editable")
    void soloBorradorEsEditable() {
        for (EstadoPregunta estado : EstadoPregunta.values()) {
            assertEquals(estado == EstadoPregunta.BORRADOR, estado.esEditable(), estado.name());
        }
    }
}
