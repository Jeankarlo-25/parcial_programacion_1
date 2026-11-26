package app;

import excepciones.PersistenciaException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import persistencia.ConexionBD;
import persistencia.HabitacionDAOJdbc;
import persistencia.ReservaDAOJdbc;
import servicio.HotelService;
import vista.VentanaPrincipal;

/** Punto de entrada de HotelLibre. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Si falla, se usa el aspecto por defecto de Swing.
            }
            try {
                ConexionBD.inicializar(); // crea hotel.db y sus tablas si no existen
            } catch (PersistenciaException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Base de datos", JOptionPane.ERROR_MESSAGE);
            }
            HotelService servicio = new HotelService(new HabitacionDAOJdbc(), new ReservaDAOJdbc());
            new VentanaPrincipal(servicio).setVisible(true);
        });
    }
}
