package modelo;

import excepciones.HabitacionInvalidaException;

/** Habitación sencilla: 1 persona, $120.000 por noche, sin minibar. */
public class Sencilla extends Habitacion {

    public static final double TARIFA_NOCHE = 120_000;

    public Sencilla(int numero, int piso) throws HabitacionInvalidaException {
        super(numero, piso);
    }

    @Override
    public TipoHabitacion getTipo() {
        return TipoHabitacion.SENCILLA;
    }

    @Override
    public int getCapacidad() {
        return 1;
    }

    @Override
    protected double tarifaNoche(int personas) {
        return TARIFA_NOCHE;
    }
}
