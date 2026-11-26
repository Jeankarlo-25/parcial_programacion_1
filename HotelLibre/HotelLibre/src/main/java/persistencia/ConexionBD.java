package persistencia;

import excepciones.PersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase de conexión a la base de datos SQLite (archivo hotel.db).
 * Cada DAO pide una conexión nueva y la cierra con try-con-recursos.
 */
public final class ConexionBD {

    private static final String URL = "jdbc:sqlite:hotel.db";
    private static final String SCRIPT = "/schema.sql";

    private ConexionBD() {
    }

    /** Abre una conexión nueva. Quien la pide la cierra con try-con-recursos. */
    public static Connection obtenerConexion() throws SQLException {
        Properties propiedades = new Properties();
        propiedades.setProperty("foreign_keys", "true"); // activa las llaves foráneas en SQLite
        return DriverManager.getConnection(URL, propiedades);
    }

    /** Crea las tablas y las habitaciones iniciales ejecutando schema.sql. */
    public static void inicializar() throws PersistenciaException {
        String script = leerScript();
        try (Connection con = obtenerConexion()) {
            for (String sentencia : script.split(";")) {
                if (!sentencia.isBlank()) {
                    try (PreparedStatement ps = con.prepareStatement(sentencia.trim())) {
                        ps.execute();
                    }
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo crear la base de datos: " + e.getMessage(), e);
        }
    }

    /** Lee el script quitando las líneas de comentario (--). */
    private static String leerScript() throws PersistenciaException {
        try (InputStream in = ConexionBD.class.getResourceAsStream(SCRIPT)) {
            if (in == null) {
                throw new PersistenciaException("No se encontró el script " + SCRIPT);
            }
            StringBuilder sb = new StringBuilder();
            for (String linea : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                int comentario = linea.indexOf("--");
                sb.append(comentario >= 0 ? linea.substring(0, comentario) : linea).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el script de la base de datos.", e);
        }
    }
}
