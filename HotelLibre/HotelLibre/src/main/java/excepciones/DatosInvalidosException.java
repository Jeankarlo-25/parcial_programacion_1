package excepciones;

/** Datos de entrada incorrectos: huésped vacío, valor de minibar inválido, habitación no seleccionada, etc. */
public class DatosInvalidosException extends HotelException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }

    public DatosInvalidosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
