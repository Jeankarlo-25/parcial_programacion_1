package modelo;

import excepciones.HabitacionInvalidaException;
import java.util.Optional;

/**
 * Habitación doble: hasta 3 personas.
 * $180.000 por noche para 2 personas, más $40.000 por noche si hay una tercera persona.
 * Tiene minibar (implementa ConMinibar).
 */
public class Doble extends Habitacion implements ConMinibar {

    public static final double TARIFA_NOCHE = 180_000;
    public static final double TARIFA_TERCERA_PERSONA = 40_000;

    public Doble(int numero, int piso) throws HabitacionInvalidaException {
        super(numero, piso);
    }

    @Override
    public TipoHabitacion getTipo() {
        return TipoHabitacion.DOBLE;
    }

    @Override
    public int getCapacidad() {
        return 3;
    }

    @Override
    protected double tarifaNoche(int personas) {
        return personas >= 3 ? TARIFA_NOCHE + TARIFA_TERCERA_PERSONA : TARIFA_NOCHE;
    }

    @Override
    public Optional<ConMinibar> getMinibar() {
        return Optional.of(this);
    }
}
