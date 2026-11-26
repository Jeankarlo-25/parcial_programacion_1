package modelo;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Reserva de una habitación.
 *
 * Su constructor NO es público: la única forma de crear una Reserva es con
 * ReservaBuilder (patrón Builder), que valida todas las reglas en build().
 * Por eso el objeto siempre nace válido y no tiene setters (es inmutable).
 */
public class Reserva {

    public static final double DESAYUNO_POR_PERSONA_NOCHE = 25_000;
    public static final double PARQUEADERO_POR_NOCHE = 15_000;
    public static final double SALIDA_TARDIA = 50_000;

    // Datos obligatorios
    private final int id;
    private final String huesped;
    private final Habitacion habitacion;
    private final LocalDate entrada;
    private final LocalDate salida;
    private final int personas;
    // Datos opcionales
    private final boolean desayuno;
    private final boolean parqueadero;
    private final boolean salidaTardia;
    private final double consumoMinibar;

    /** Solo lo llama ReservaBuilder.build() (mismo paquete), después de validar. */
    Reserva(ReservaBuilder b) {
        this.id = b.getId();
        this.huesped = b.getHuesped();
        this.habitacion = b.getHabitacion();
        this.entrada = b.getEntrada();
        this.salida = b.getSalida();
        this.personas = b.getPersonas();
        this.desayuno = b.isDesayuno();
        this.parqueadero = b.isParqueadero();
        this.salidaTardia = b.isSalidaTardia();
        this.consumoMinibar = b.getConsumoMinibar();
    }

    // ------------------- Cálculos -------------------

    public long getNoches() {
        return ChronoUnit.DAYS.between(entrada, salida);
    }

    /** Alojamiento: lo calcula la habitación según su tipo (polimorfismo). */
    public double getAlojamiento() {
        return habitacion.calcularAlojamiento(entrada, salida, personas);
    }

    /** Alojamiento de las noches que caen en un mes (consulta R4-b). */
    public double getAlojamientoEnMes(YearMonth mes) {
        return habitacion.calcularAlojamientoEnMes(entrada, salida, personas, mes);
    }

    /** Desayuno ($25.000 por persona por noche) + parqueadero ($15.000 por noche) + salida tardía ($50.000). */
    public double getOpcionales() {
        double total = 0;
        if (desayuno) {
            total += DESAYUNO_POR_PERSONA_NOCHE * personas * getNoches();
        }
        if (parqueadero) {
            total += PARQUEADERO_POR_NOCHE * getNoches();
        }
        if (salidaTardia) {
            total += SALIDA_TARDIA;
        }
        return total;
    }

    /** Minibar con IVA del 19 %. Si la habitación no tiene minibar (sencilla) da 0. */
    public double getMinibar() {
        return habitacion.getMinibar()
                .map(minibar -> minibar.calcularMinibar(consumoMinibar))
                .orElse(0.0);
    }

    public double getTotal() {
        return getAlojamiento() + getOpcionales() + getMinibar();
    }

    /** true si las fechas de esta reserva se cruzan con [entrada, salida). El día de salida queda libre. */
    public boolean seCruzaCon(LocalDate otraEntrada, LocalDate otraSalida) {
        return otraEntrada.isBefore(salida) && otraSalida.isAfter(entrada);
    }

    /** Lista legible de los opcionales elegidos. */
    public String getServiciosTexto() {
        List<String> s = new ArrayList<>();
        if (desayuno) {
            s.add("Desayuno");
        }
        if (parqueadero) {
            s.add("Parqueadero");
        }
        if (salidaTardia) {
            s.add("Salida tardía");
        }
        return s.isEmpty() ? "Ninguno" : String.join(", ", s);
    }

    // ------------------- Getters -------------------

    public int getId() {
        return id;
    }

    public String getHuesped() {
        return huesped;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public LocalDate getEntrada() {
        return entrada;
    }

    public LocalDate getSalida() {
        return salida;
    }

    public int getPersonas() {
        return personas;
    }

    public boolean isDesayuno() {
        return desayuno;
    }

    public boolean isParqueadero() {
        return parqueadero;
    }

    public boolean isSalidaTardia() {
        return salidaTardia;
    }

    public double getConsumoMinibar() {
        return consumoMinibar;
    }
}
