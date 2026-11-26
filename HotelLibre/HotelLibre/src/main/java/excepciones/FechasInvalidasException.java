package excepciones;

/** Fecha de entrada/salida mal escrita o salida que no es posterior a la entrada. */
public class FechasInvalidasException extends HotelException {

    public FechasInvalidasException(String mensaje) {
        super(mensaje);
    }

    public FechasInvalidasException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
