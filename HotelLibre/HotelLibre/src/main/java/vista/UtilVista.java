package vista;

import excepciones.DatosInvalidosException;
import excepciones.FechasInvalidasException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import javax.swing.table.DefaultTableModel;

/** Utilidades de la vista: leer lo que escribe el usuario y dar formato a los valores. */
final class UtilVista {

    private static final DecimalFormat PESOS;

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        PESOS = new DecimalFormat("$ #,##0", simbolos);
    }

    private UtilVista() {
    }

    /** 1485000.0 → "$ 1.485.000" */
    static String pesos(double valor) {
        return PESOS.format(Math.round(valor));
    }

    static LocalDate leerFecha(String texto, String campo) throws FechasInvalidasException {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new FechasInvalidasException("La " + campo + " \"" + texto.trim()
                    + "\" no es válida. Use el formato aaaa-mm-dd (ej. 2026-12-15).");
        }
    }

    static YearMonth leerMes(String texto) throws FechasInvalidasException {
        try {
            return YearMonth.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new FechasInvalidasException("El mes \"" + texto.trim() + "\" no es válido. Use aaaa-mm (ej. 2026-12).");
        }
    }

    static int leerEntero(String texto, String campo) throws DatosInvalidosException {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El campo " + campo + " debe ser un número entero.");
        }
    }

    /** Acepta "85000", "85.000" o "$ 85.000". La coma se toma como decimal. */
    static double leerDinero(String texto, String campo) throws DatosInvalidosException {
        String limpio = texto.trim().replace("$", "").replace(" ", "").replace(".", "").replace(',', '.');
        if (limpio.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El campo " + campo + " debe ser un valor en pesos (ej. 85000).");
        }
    }

    /** Modelo de tabla de solo lectura. */
    static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }
}
