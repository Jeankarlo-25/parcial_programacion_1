package excepciones;

/** Número de habitación que no tiene 3 dígitos, piso que no coincide con el primer dígito, o habitación repetida. */
public class HabitacionInvalidaException extends HotelException {

    public HabitacionInvalidaException(String mensaje) {
        super(mensaje);
    }

    public HabitacionInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
