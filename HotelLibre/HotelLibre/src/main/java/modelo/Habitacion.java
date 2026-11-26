package modelo;

import excepciones.HabitacionInvalidaException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * CLASE ABSTRACTA: representa cualquier habitación del hotel.
 * No se puede instanciar; solo existen Sencilla, Doble y Suite.
 *
 * El cálculo del alojamiento usa POLIMORFISMO: este método recorre la estadía
 * noche por noche y le pide a cada subclase su tarifa (tarifaNoche) y su
 * descuento (aplicarDescuento). Nunca se pregunta "qué tipo soy".
 */
public abstract class Habitacion {

    /** Recargo de temporada alta (25 %) sobre la tarifa de esa noche. */
    public static final double RECARGO_TEMPORADA_ALTA = 0.25;
    private static final MonthDay INICIO_TEMPORADA_ALTA = MonthDay.of(12, 15);
    private static final MonthDay FIN_TEMPORADA_ALTA = MonthDay.of(1, 15);

    // Atributos privados: solo se asignan después de validarse.
    private final int numero;
    private final int piso;

    protected Habitacion(int numero, int piso) throws HabitacionInvalidaException {
        validar(numero, piso);
        this.numero = numero;
        this.piso = piso;
    }

    /** Número de 3 dígitos y su primer dígito es el piso (ej. 310 → piso 3). */
    private static void validar(int numero, int piso) throws HabitacionInvalidaException {
        if (numero < 100 || numero > 999) {
            throw new HabitacionInvalidaException(
                    "El número de habitación debe tener tres dígitos (100 a 999). Se recibió: " + numero);
        }
        if (numero / 100 != piso) {
            throw new HabitacionInvalidaException("La habitación " + numero + " debe estar en el piso "
                    + (numero / 100) + " (primer dígito del número), no en el piso " + piso + ".");
        }
    }

    // ------------------- Métodos que cada subclase resuelve -------------------

    /** Tipo de la habitación (cada subclase devuelve el suyo). */
    public abstract TipoHabitacion getTipo();

    /** Capacidad máxima de personas: sencilla 1, doble 3, suite 4. */
    public abstract int getCapacidad();

    /** Tarifa de UNA noche normal (sin temporada alta) para esa cantidad de personas. */
    protected abstract double tarifaNoche(int personas);

    /** Descuento sobre el alojamiento. Por defecto no hay; la Suite lo sobrescribe. */
    protected double aplicarDescuento(double alojamiento, long nochesTotales) {
        return alojamiento;
    }

    /**
     * Minibar: por defecto la habitación NO tiene (sencilla).
     * Doble y Suite implementan la interface ConMinibar y sobrescriben este
     * método devolviéndose a sí mismas. Así nadie necesita usar instanceof.
     */
    public Optional<ConMinibar> getMinibar() {
        return Optional.empty();
    }

    // ------------------- Cálculo del alojamiento (noche por noche) -------------------

    /** Valor del alojamiento de toda la estadía. */
    public final double calcularAlojamiento(LocalDate entrada, LocalDate salida, int personas) {
        return calcular(entrada, salida, personas, noche -> true);
    }

    /** Valor del alojamiento solo de las noches que caen en el mes indicado (consulta R4-b). */
    public final double calcularAlojamientoEnMes(LocalDate entrada, LocalDate salida, int personas,
            YearMonth mes) {
        return calcular(entrada, salida, personas, noche -> YearMonth.from(noche).equals(mes));
    }

    private double calcular(LocalDate entrada, LocalDate salida, int personas, Predicate<LocalDate> incluir) {
        long nochesTotales = ChronoUnit.DAYS.between(entrada, salida);
        double total = 0;
        for (LocalDate noche = entrada; noche.isBefore(salida); noche = noche.plusDays(1)) {
            if (incluir.test(noche)) {
                double tarifa = tarifaNoche(personas);           // polimorfismo
                if (esTemporadaAlta(noche)) {
                    tarifa += tarifa * RECARGO_TEMPORADA_ALTA;
                }
                total += tarifa;
            }
        }
        return aplicarDescuento(total, nochesTotales);          // polimorfismo
    }

    /** Del 15 de diciembre al 15 de enero, ambos incluidos. */
    public static boolean esTemporadaAlta(LocalDate fecha) {
        MonthDay dia = MonthDay.from(fecha);
        return !dia.isBefore(INICIO_TEMPORADA_ALTA) || !dia.isAfter(FIN_TEMPORADA_ALTA);
    }

    // ------------------- Getters -------------------

    public int getNumero() {
        return numero;
    }

    public int getPiso() {
        return piso;
    }

    @Override
    public String toString() {
        return numero + " - " + getTipo().getNombre() + " (cap. " + getCapacidad() + ")";
    }
}
