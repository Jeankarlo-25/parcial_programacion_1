package modelo;

import excepciones.HabitacionInvalidaException;

/**
 * Tipos de habitación. Cada tipo sabe crear un objeto de SU subclase
 * (Sencilla::new, Doble::new, Suite::new). Gracias a esto, al leer de la base
 * de datos se reconstruye la clase correcta sin usar switch ni if sobre el tipo:
 *
 *     TipoHabitacion.valueOf("SUITE").crear(501, 5)  →  new Suite(501, 5)
 */
public enum TipoHabitacion {

    SENCILLA("Sencilla", Sencilla::new),
    DOBLE("Doble", Doble::new),
    SUITE("Suite", Suite::new);

    /** Interface funcional propia porque el constructor lanza una excepción verificada. */
    @FunctionalInterface
    private interface Creador {
        Habitacion crear(int numero, int piso) throws HabitacionInvalidaException;
    }

    private final String nombre;
    private final Creador creador;

    TipoHabitacion(String nombre, Creador creador) {
        this.nombre = nombre;
        this.creador = creador;
    }

    public Habitacion crear(int numero, int piso) throws HabitacionInvalidaException {
        return creador.crear(numero, piso);
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
