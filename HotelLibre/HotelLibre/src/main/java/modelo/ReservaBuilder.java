package modelo;

import excepciones.CapacidadExcedidaException;
import excepciones.DatosInvalidosException;
import excepciones.FechasInvalidasException;
import excepciones.HabitacionOcupadaException;
import excepciones.HotelException;
import java.time.LocalDate;
import java.util.List;

/**
 * PATRÓN BUILDER (variante 4).
 *
 * - El constructor recibe los datos OBLIGATORIOS: huésped, habitación, fechas y personas.
 * - Los OPCIONALES se agregan en cualquier orden: conDesayuno(), conParqueadero(),
 *   conSalidaTardia(), conMinibar(consumo).
 * - build() valida TODAS las reglas antes de crear la Reserva.
 *
 * Ejemplo (caso de prueba 2):
 *   new ReservaBuilder("Ana", hab310, entrada, salida, 3)
 *       .conParqueadero().conDesayuno().conMinibar(85000)
 *       .build();
 *
 * Se usa en el flujo real de la aplicación: la ventana lo usa al guardar/editar
 * y el DAO lo usa al reconstruir las reservas leídas de la base de datos.
 */
public class ReservaBuilder {

    // Obligatorios
    private final String huesped;
    private final Habitacion habitacion;
    private final LocalDate entrada;
    private final LocalDate salida;
    private final int personas;

    // Opcionales (con su valor por defecto)
    private int id;
    private boolean desayuno;
    private boolean parqueadero;
    private boolean salidaTardia;
    private double consumoMinibar;
    private List<Reserva> reservasExistentes = List.of();

    public ReservaBuilder(String huesped, Habitacion habitacion, LocalDate entrada, LocalDate salida,
            int personas) {
        this.huesped = huesped == null ? "" : huesped.trim();
        this.habitacion = habitacion;
        this.entrada = entrada;
        this.salida = salida;
        this.personas = personas;
    }

    public ReservaBuilder conDesayuno() {
        this.desayuno = true;
        return this;
    }

    public ReservaBuilder conParqueadero() {
        this.parqueadero = true;
        return this;
    }

    public ReservaBuilder conSalidaTardia() {
        this.salidaTardia = true;
        return this;
    }

    public ReservaBuilder conMinibar(double consumo) {
        this.consumoMinibar = consumo;
        return this;
    }

    /** Id de la reserva (se usa al editar y al reconstruir desde la base de datos). */
    public ReservaBuilder conId(int id) {
        this.id = id;
        return this;
    }

    /** Reservas ya guardadas, para comprobar que las fechas no se crucen. */
    public ReservaBuilder verificandoContra(List<Reserva> existentes) {
        this.reservasExistentes = existentes == null ? List.of() : existentes;
        return this;
    }

    /** Valida las reglas del negocio y, si todo está bien, crea la reserva. */
    public Reserva build() throws HotelException {
        if (huesped.isEmpty()) {
            throw new DatosInvalidosException("Escriba el nombre del huésped.");
        }
        if (habitacion == null) {
            throw new DatosInvalidosException("Seleccione una habitación.");
        }
        validarFechas();
        validarCapacidad();
        validarMinibar();
        validarDisponibilidad();
        return new Reserva(this);
    }

    private void validarFechas() throws FechasInvalidasException {
        if (entrada == null || salida == null) {
            throw new FechasInvalidasException("Debe indicar la fecha de entrada y la de salida.");
        }
        if (!salida.isAfter(entrada)) {
            throw new FechasInvalidasException("La fecha de salida (" + salida
                    + ") debe ser posterior a la fecha de entrada (" + entrada + ").");
        }
    }

    private void validarCapacidad() throws CapacidadExcedidaException {
        if (personas < 1 || personas > habitacion.getCapacidad()) {
            throw new CapacidadExcedidaException("La habitación " + habitacion.getNumero() + " ("
                    + habitacion.getTipo().getNombre() + ") admite máximo " + habitacion.getCapacidad()
                    + " persona(s) y mínimo 1. Se pidieron " + personas + ".");
        }
    }

    private void validarMinibar() throws DatosInvalidosException {
        if (consumoMinibar < 0) {
            throw new DatosInvalidosException("El consumo de minibar no puede ser negativo.");
        }
        if (consumoMinibar > 0 && habitacion.getMinibar().isEmpty()) {
            throw new DatosInvalidosException("La habitación " + habitacion.getNumero() + " ("
                    + habitacion.getTipo().getNombre() + ") no tiene minibar.");
        }
    }

    private void validarDisponibilidad() throws HabitacionOcupadaException {
        for (Reserva otra : reservasExistentes) {
            boolean esLaMisma = otra.getId() == id && id != 0;
            boolean mismaHabitacion = otra.getHabitacion().getNumero() == habitacion.getNumero();
            if (!esLaMisma && mismaHabitacion && otra.seCruzaCon(entrada, salida)) {
                throw new HabitacionOcupadaException("La habitación " + habitacion.getNumero()
                        + " ya está reservada del " + otra.getEntrada() + " al " + otra.getSalida()
                        + " (huésped: " + otra.getHuesped() + "). Las fechas se cruzan.");
            }
        }
    }

    // Getters de paquete: solo los usa el constructor de Reserva.
    int getId() {
        return id;
    }

    String getHuesped() {
        return huesped;
    }

    Habitacion getHabitacion() {
        return habitacion;
    }

    LocalDate getEntrada() {
        return entrada;
    }

    LocalDate getSalida() {
        return salida;
    }

    int getPersonas() {
        return personas;
    }

    boolean isDesayuno() {
        return desayuno;
    }

    boolean isParqueadero() {
        return parqueadero;
    }

    boolean isSalidaTardia() {
        return salidaTardia;
    }

    double getConsumoMinibar() {
        return consumoMinibar;
    }
}
