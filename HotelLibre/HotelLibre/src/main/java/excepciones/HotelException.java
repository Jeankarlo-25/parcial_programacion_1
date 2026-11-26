package excepciones;

/**
 * Excepción base (propia) del sistema HotelLibre.
 * Todas las excepciones del proyecto heredan de esta, así la ventana puede
 * capturarlas con un solo catch y mostrar el mensaje sin cerrarse.
 */
public class HotelException extends Exception {

    public HotelException(String mensaje) {
        super(mensaje);
    }

    public HotelException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
