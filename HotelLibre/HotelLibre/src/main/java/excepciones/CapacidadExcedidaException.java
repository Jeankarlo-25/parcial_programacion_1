package excepciones;

/** Número de personas fuera del rango 1..capacidad de la habitación. */
public class CapacidadExcedidaException extends HotelException {

    public CapacidadExcedidaException(String mensaje) {
        super(mensaje);
    }

    public CapacidadExcedidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
