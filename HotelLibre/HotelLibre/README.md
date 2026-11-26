# HotelLibre — Gestión de reservas de hotel

Parcial del tercer corte de **Programación 1**, Universidad Libre, docente Andrés Castaño.

| Dato | Valor |
|---|---|
| **Estudiante** | George Jeancarlo Caicedo Lopez |
| **Código** | 1011201984 |
| **Variante** | **4 — Hotel HotelLibre** |
| **Patrón de diseño** | **Builder** (`modelo.ReservaBuilder`) |
| **Motor de base de datos** | **SQLite** (JDBC, driver `org.xerial:sqlite-jdbc`) |
| **Interfaz gráfica** | Java Swing |

## Cómo ejecutar la aplicación

**Requisitos:** JDK 17 o superior y Maven. NetBeans e IntelliJ ya traen Maven. No hay que instalar ningún servidor de base de datos.

```bash
git clone <URL-de-este-repositorio>
cd HotelLibre
mvn compile exec:java
```

En **NetBeans** vaya a *File → Open Project*, elija la carpeta `HotelLibre` y luego *Run*. La clase principal es `app.Main`.

La **primera vez** que se ejecuta, la aplicación crea sola el archivo `hotel.db` en la carpeta del proyecto. Para hacerlo corre el script [`src/main/resources/schema.sql`](src/main/resources/schema.sql), que crea las tablas y carga 14 habitaciones del hotel (entre ellas 204, 310, 501 y 502, que se usan en los casos de prueba). Si borra `hotel.db`, la base empieza de cero.

## Base de datos (R1)

Script: **[`src/main/resources/schema.sql`](src/main/resources/schema.sql)**. Tiene 2 tablas, por debajo del máximo de 3:

| Tabla | Columnas | Llaves |
|---|---|---|
| `habitacion` | numero, piso, tipo (SENCILLA/DOBLE/SUITE), capacidad | PK `numero` |
| `reserva` | id, huesped, numero_habitacion, fecha_entrada, fecha_salida, personas, desayuno, parqueadero, salida_tardia, consumo_minibar | PK `id`, FK `numero_habitacion → habitacion(numero)` |

Los valores calculados (alojamiento, opcionales, minibar y total) **no se guardan**: se recalculan al leer, así que dan el mismo resultado antes y después de guardar (R3).

## Estructura de paquetes

| Paquete | Contenido |
|---|---|
| `app` | `Main`: punto de entrada |
| `modelo` | `Habitacion` (abstracta), `Sencilla`, `Doble`, `Suite`, interface `ConMinibar`, `TipoHabitacion`, `Reserva`, **`ReservaBuilder`** |
| `excepciones` | `HotelException` (base) y sus hijas: `FechasInvalidasException`, `CapacidadExcedidaException`, `HabitacionOcupadaException`, `HabitacionInvalidaException`, `DatosInvalidosException`, `PersistenciaException` |
| `persistencia` | `ConexionBD`, interfaces `HabitacionDAO` y `ReservaDAO`, y sus implementaciones `HabitacionDAOJdbc` y `ReservaDAOJdbc` |
| `servicio` | `HotelService`: une modelo y DAO (la ventana solo habla con esta clase) |
| `vista` | `VentanaPrincipal` (pestañas Reservas, Habitaciones y Consultas) y `UtilVista` |

## Dónde se cumple cada requisito

| Req. | Dónde | Detalle |
|---|---|---|
| **R1** | `schema.sql` | 2 tablas con PK, FK y CHECK |
| **R2** | `persistencia.*` | `ConexionBD` + interfaces DAO + implementaciones JDBC. Todo usa `PreparedStatement` y `try` con recursos (también las conexiones). Los `SQLException` se convierten en `PersistenciaException`, que la ventana muestra como mensaje sin cerrarse |
| **R3** | `HabitacionDAOJdbc.convertir` y `ReservaDAOJdbc.convertir` | `TipoHabitacion.valueOf(tipo).crear(numero, piso)` crea `Sencilla`, `Doble` o `Suite` sin `switch`, y la reserva se reconstruye con el mismo `ReservaBuilder` |
| **R4** | `ReservaDAO.ocupadasEnFecha` y `ReservaDAO.conNochesEnMes` | a) ocupadas en una fecha (`entrada <= fecha < salida`); b) reservas con noches en el mes, que `HotelService.ingresosPorTipo` agrupa por tipo sumando solo las noches de ese mes |
| **R5** | Pestaña *Reservas* | Tabla con tipo, alojamiento, opcionales, minibar (+IVA) y total |
| **R6** | Pestaña *Reservas* | El **tipo** elegido filtra las habitaciones. El campo **minibar** solo se habilita si la habitación es doble o suite |
| **R7** | Pestaña *Reservas* | Al hacer clic en una fila los datos pasan al formulario; se modifican y se usa **Guardar cambios** |
| **R8** | Pestaña *Reservas* | El botón **Eliminar** pide confirmación antes de borrar |
| **R9** | Pestaña *Consultas* | a) campo de fecha y botón; b) campo de mes y botón |
| **R10** | `modelo` / `excepciones` | Ver la tabla siguiente |
| **R11** | `modelo.ReservaBuilder` | Ver la sección del patrón Builder |

### Conceptos de POO (R10)

| Concepto | Dónde |
|---|---|
| **Clase abstracta y herencia** | `Habitacion` es abstracta, con los métodos abstractos `getTipo()`, `getCapacidad()` y `tarifaNoche()`. `Sencilla`, `Doble` y `Suite` la extienden |
| **Encapsulamiento y validación** | Los atributos son `private`. El constructor de `Habitacion` valida que el número tenga 3 dígitos y que su primer dígito sea el piso **antes** de asignar. `Reserva` no tiene setters: solo se crea con `ReservaBuilder.build()`, después de validar |
| **Polimorfismo** | `Habitacion.calcularAlojamiento()` recorre la estadía noche por noche y llama a `tarifaNoche()` y `aplicarDescuento()`, que cada subclase sobrescribe: la Suite aplica 10 % desde 5 noches y la Doble suma $40.000 por la tercera persona. **No hay `instanceof` ni `switch` sobre el tipo** |
| **Interface `ConMinibar`** | Solo la implementan `Doble` y `Suite`: consumo + 19 % de IVA. `Habitacion.getMinibar()` devuelve `Optional.empty()` en la sencilla y `Optional.of(this)` en doble y suite, así que nunca se pregunta por el tipo |
| **Excepciones propias** | Una por cada validación: `FechasInvalidasException` (salida no posterior a la entrada o fecha mal escrita), `CapacidadExcedidaException` (personas fuera de 1..capacidad), `HabitacionOcupadaException` (fechas cruzadas), `HabitacionInvalidaException` (número o piso inválidos, o habitación repetida), `DatosInvalidosException` (huésped vacío, minibar negativo o en una sencilla) y `PersistenciaException` (errores de base de datos) |

### Patrón Builder (R11)

`ReservaBuilder` recibe en el constructor los datos **obligatorios**: huésped, habitación, fechas y personas. Los **opcionales** se agregan en cualquier orden:

```java
Reserva r = new ReservaBuilder("Carlos Ruiz", hab310, entrada, salida, 3)
        .conParqueadero()
        .conDesayuno()
        .conMinibar(85000)
        .build();          // valida fechas, capacidad, minibar y cruce de fechas
```

* `build()` valida todas las reglas antes de crear la `Reserva`. El constructor de `Reserva` es de paquete, así que **no hay otra forma de crear una reserva**.
* El builder se usa en el flujo real de la aplicación:
  * `VentanaPrincipal.leerFormulario()` arma el builder con los opcionales marcados.
  * `HotelService` le pasa las reservas existentes de esa habitación (`verificandoContra`) para detectar cruces de fechas, y luego llama a `build()`.
  * `ReservaDAOJdbc.convertir()` reconstruye cada reserva leída de la base de datos con el mismo builder.

## Reglas de cálculo

* **Alojamiento**, calculado noche por noche:
  * Sencilla: $120.000.
  * Doble: $180.000, más $40.000 si hay una tercera persona.
  * Suite: $350.000, con 10 % de descuento sobre el alojamiento si la estadía es de 5 noches o más.
  * Las noches del 15-dic al 15-ene (ambos incluidos) tienen un recargo del 25 % sobre la tarifa de esa noche.
* **Opcionales:**
  * Desayuno: $25.000 por persona por noche.
  * Parqueadero: $15.000 por noche.
  * Salida tardía: $50.000.
* **Minibar** (solo doble y suite): consumo × 1,19.
* **Total** = alojamiento + opcionales + minibar.
* Dos reservas de la misma habitación se cruzan si `entradaNueva < salidaExistente` y `salidaNueva > entradaExistente`. Por eso una reserva puede empezar el mismo día en que termina otra.

## Casos de prueba y resultados esperados

| # | Caso | Alojamiento | Opcionales | Minibar | **Total** |
|---|---|---|---|---|---|
| 1 | Sencilla 204, 2026-11-20 → 11-23, 1 persona, desayuno | $360.000 | $75.000 | $0 | **$435.000** |
| 2 | Doble 310, 2026-12-12 → 12-18, 3 personas, desayuno + parqueadero, minibar $85.000 | $1.485.000 | $540.000 | $101.150 | **$2.126.150** |
| 3 | Suite 501, 2026-12-28 → 2027-01-03, 4 personas, salida tardía, minibar $140.000 | $2.362.500 | $50.000 | $166.600 | **$2.579.100** |
| 4 | Suite 502, 2026-11-25 → 11-28, 2 personas, sin opcionales | $1.050.000 | $0 | $0 | **$1.050.000** |

Detalle de los cálculos:

* **Caso 2:** 3 noches normales × $220.000 + 3 noches de temporada alta (15, 16 y 17 de diciembre) × $275.000.
* **Caso 3:** 6 noches de temporada alta × $437.500 = $2.625.000, menos el 10 % por ser 5 noches o más.

Casos de error (se muestran como mensaje y la ventana sigue abierta):

| # | Caso | Resultado |
|---|---|---|
| 5 | Reservar la 204 para 2 personas | `CapacidadExcedidaException` |
| 6 | Reservar la 310 del 2026-12-17 al 2026-12-20 | `HabitacionOcupadaException` (se cruza con el caso 2) |
| 7 | Fecha de salida igual a la de entrada | `FechasInvalidasException` |
| 8 | Cerrar y volver a abrir | Las 4 reservas siguen en la tabla con los mismos valores (SQLite + reconstrucción) |

Consultas de ejemplo:

* **a)** Fecha 2026-12-31: aparece la habitación 501.
* **b)** Mes 2026-12: Doble $1.485.000 y Suite $1.575.000, es decir, 4 noches de diciembre de la 501. Mes 2027-01: Suite $787.500, las 2 noches de enero.
