package modelo;

/**
 * INTERFACE de Java de la variante 4.
 * La implementan SOLO Doble y Suite. La Sencilla no tiene minibar.
 * El consumo de minibar se suma al total con IVA del 19 % aplicado solo al minibar.
 */
public interface ConMinibar {

    double IVA_MINIBAR = 0.19;

    /** Consumo de minibar + 19 % de IVA. */
    default double calcularMinibar(double consumo) {
        return consumo + consumo * IVA_MINIBAR;
    }
}
