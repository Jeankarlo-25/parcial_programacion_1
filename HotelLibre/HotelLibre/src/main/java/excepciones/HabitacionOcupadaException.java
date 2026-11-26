package excepciones;

/** La habitación ya tiene una reserva con fechas que se cruzan. */
public class HabitacionOcupadaException extends HotelException {

    public HabitacionOcupadaException(String mensaje) {
        super(mensaje);
    }

    public HabitacionOcupadaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
