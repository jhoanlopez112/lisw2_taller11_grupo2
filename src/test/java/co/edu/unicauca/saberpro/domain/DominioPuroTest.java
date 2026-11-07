package co.edu.unicauca.saberpro.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifica automáticamente que la capa de dominio siga siendo Java puro:
 * sin Spring, sin JPA ni otras dependencias técnicas.
 */
@DisplayName("Aislamiento de la capa de dominio")
class DominioPuroTest {

    private static final Path CODIGO_DEL_DOMINIO =
            Path.of("src", "main", "java", "co", "edu", "unicauca", "saberpro", "domain");

    private static final List<String> TEXTOS_PROHIBIDOS = List.of(
            "org.springframework", "jakarta.", "javax.persistence", "lombok", "org.hibernate");

    @Test
    @DisplayName("El dominio solo importa clases de java.* y del propio dominio")
    void soloImportaJavaYDominio() {
        List<String> violaciones = lineasDelDominio()
                .filter(linea -> linea.startsWith("import "))
                .filter(linea -> !importPermitido(linea))
                .toList();
        assertTrue(violaciones.isEmpty(), () -> "Imports no permitidos: " + violaciones);
    }

    @Test
    @DisplayName("El dominio no menciona Spring, JPA, Lombok ni Hibernate")
    void noMencionaFrameworks() {
        List<String> violaciones = lineasDelDominio()
                .filter(linea -> TEXTOS_PROHIBIDOS.stream().anyMatch(linea::contains))
                .toList();
        assertTrue(violaciones.isEmpty(), () -> "Referencias a frameworks: " + violaciones);
    }

    @Test
    @DisplayName("Las únicas anotaciones del dominio son @Override")
    void sinAnotacionesDeFrameworks() {
        List<String> anotaciones = lineasDelDominio()
                .filter(linea -> linea.startsWith("@"))
                .filter(linea -> !linea.equals("@Override"))
                .toList();
        assertTrue(anotaciones.isEmpty(), () -> "Anotaciones no permitidas: " + anotaciones);
    }

    private static boolean importPermitido(String linea) {
        String tipo = linea.replaceFirst("^import\\s+(static\\s+)?", "");
        return tipo.startsWith("java.") || tipo.startsWith("co.edu.unicauca.saberpro.domain.");
    }

    private static Stream<String> lineasDelDominio() {
        assertTrue(Files.isDirectory(CODIGO_DEL_DOMINIO),
                "Ejecute las pruebas desde la raíz del proyecto: " + CODIGO_DEL_DOMINIO.toAbsolutePath());
        List<Path> archivos;
        try (Stream<Path> recorrido = Files.walk(CODIGO_DEL_DOMINIO)) {
            archivos = recorrido.filter(p -> p.toString().endsWith(".java")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertFalse(archivos.isEmpty(), "No se encontraron clases del dominio");
        return archivos.stream().flatMap(DominioPuroTest::leerLineas).map(String::trim);
    }

    private static Stream<String> leerLineas(Path archivo) {
        try {
            return Files.readAllLines(archivo, StandardCharsets.UTF_8).stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
