# Taller 11 — Domain-Driven Design (DDD) · Banco de Preguntas Saber PRO

Laboratorio de Ingeniería de Software II — Universidad del Cauca (2026.2)
Profesora: Ing. Paola Andrea Bedoya Toro

**Grupo 2:** Johan Santiago López · Eddy Emmanuel Sánchez

Capa de dominio del Banco de Preguntas Saber PRO escrita en **Java puro (17+)**:
sin Spring, sin JPA y sin anotaciones de frameworks. Implementa el agregado
`Pregunta`, sus objetos de valor inmutables y las invariantes del negocio, con
pruebas en JUnit 5.

## Diagramas

- [Mapa de contextos](docs/mapa-de-contextos.png): Autoría, Validación, Ciclo de Vida y Revisión por Pares.
- [Diagrama de clases](docs/diagrama-de-clases.png): agregado `Pregunta` y su límite de consistencia.
- [Diagrama de estados](docs/diagrama-de-estados.png): ciclo de vida de `EstadoPregunta`.

## Estructura

```
src/main/java/co/edu/unicauca/saberpro/domain/
├── model/
│   ├── Pregunta.java            # RAÍZ DEL AGREGADO (entidad con identidad)
│   ├── Enunciado.java           # OBJETO DE VALOR inmutable (no nulo ni vacío)
│   ├── Justificacion.java       # OBJETO DE VALOR inmutable (≥ 15 caracteres, sin expresiones prohibidas)
│   ├── OpcionRespuesta.java     # OBJETO DE VALOR inmutable (texto + indicador de clave)
│   └── EstadoPregunta.java      # ENUM con las transiciones del ciclo de vida
├── exception/
│   ├── DomainException.java     # Excepción base del dominio
│   └── ReglaNegocioException.java
└── repository/
    └── PreguntaRepository.java  # Interfaz pura (puerto); la implementa la infraestructura

src/test/java/co/edu/unicauca/saberpro/domain/
├── model/        PreguntaTest, EnunciadoTest, JustificacionTest, OpcionRespuestaTest, EstadoPreguntaTest
├── repository/   PreguntaRepositoryTest + PreguntaRepositoryEnMemoria (implementación de prueba)
└── DominioPuroTest.java   # Verifica que el dominio no importe nada fuera de java.* y del propio dominio
```

## Invariantes del agregado `Pregunta`

| # | Regla | Dónde se garantiza |
|---|-------|--------------------|
| 1 | El enunciado no puede ser nulo ni vacío | `Enunciado` |
| 2 | La justificación tiene al menos 15 caracteres (sin contar espacios de los extremos) | `Justificacion` |
| 3 | La justificación no usa expresiones prohibidas (*obviamente*, *evidentemente*, *es obvio*, *porque sí*, *lorem ipsum*) | `Justificacion` |
| 4 | Una opción de respuesta tiene texto | `OpcionRespuesta` |
| 5 | Nunca más de 4 opciones ni dos opciones con el mismo texto | `Pregunta.agregarOpcion` |
| 6 | Para enviar a revisión: enunciado, justificación, **exactamente 4 opciones** y **exactamente 1 clave** | `Pregunta.enviarARevision` |
| 7 | El contenido solo se modifica en estado `BORRADOR` | `Pregunta` + `EstadoPregunta.esEditable` |
| 8 | Los cambios de estado siguen la tabla de transiciones | `EstadoPregunta.puedeTransitarA` |
| 9 | La identidad es positiva y no cambia una vez asignada | `Pregunta.asignarId` |

Toda violación lanza `ReglaNegocioException` (subclase de `DomainException`).

## Ciclo de vida

| Estado origen | Acción | Estado destino |
|---------------|--------|----------------|
| BORRADOR | `enviarARevision()` (exige las invariantes) | PENDIENTE_REVISION |
| PENDIENTE_REVISION | `iniciarRevision()` | EN_REVISION |
| PENDIENTE_REVISION | `devolverABorrador()` | BORRADOR |
| EN_REVISION | `aprobar()` | APROBADA |
| EN_REVISION | `rechazar()` | RECHAZADA |
| RECHAZADA | `devolverABorrador()` | BORRADOR |
| APROBADA | `publicar()` | PUBLICADA |
| PUBLICADA | `archivar()` | ARCHIVADA (final) |

Cualquier otra transición lanza `ReglaNegocioException`.

## Cómo ejecutar las pruebas

Requisitos: JDK 17 o superior y Maven 3.9+.

```bash
mvn test
```

Resultado esperado: **130 ejecuciones de prueba, 0 fallos** (57 métodos de
prueba; los parametrizados se ejecutan una vez por cada caso).

El único `dependency` del `pom.xml` es `junit-jupiter` con `<scope>test</scope>`:
el código de `src/main` compila solo con el JDK.
