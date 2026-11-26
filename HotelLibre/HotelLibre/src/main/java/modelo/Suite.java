package modelo;

import excepciones.HabitacionInvalidaException;
import java.util.Optional;

/**
 * Suite: hasta 4 personas, $350.000 por noche.
 * Si la reserva es de 5 noches o más, 10 % de descuento sobre el alojamiento.
 * Tiene minibar (implementa ConMinibar).
 */
public class Suite extends Habitacion implements ConMinibar {

    public static final double TARIFA_NOCHE = 350_000;
    public static final int NOCHES_PARA_DESCUENTO = 5;
    public static final double DESCUENTO = 0.10;

    public Suite(int numero, int piso) throws HabitacionInvalidaException {
        super(numero, piso);
    }

    @Override
    public TipoHabitacion getTipo() {
        return TipoHabitacion.SUITE;
    }

    @Override
    public int getCapacidad() {
        return 4;
    }

    @Override
    protected double tarifaNoche(int personas) {
        return TARIFA_NOCHE;
    }

    @Override
    protected double aplicarDescuento(double alojamiento, long nochesTotales) {
        return nochesTotales >= NOCHES_PARA_DESCUENTO ? alojamiento - alojamiento * DESCUENTO : alojamiento;
    }

    @Override
    public Optional<ConMinibar> getMinibar() {
        return Optional.of(this);
    }
}
