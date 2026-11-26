package excepciones;

/** Cualquier error de la base de datos (se muestra como mensaje y no cierra la aplicación). */
public class PersistenciaException extends HotelException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
