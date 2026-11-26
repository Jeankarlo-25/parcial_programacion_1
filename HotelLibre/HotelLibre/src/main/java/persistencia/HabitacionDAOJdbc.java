package persistencia;

import excepciones.HabitacionInvalidaException;
import excepciones.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import modelo.Habitacion;
import modelo.TipoHabitacion;

/** Implementación JDBC (SQLite) de HabitacionDAO. */
public class HabitacionDAOJdbc implements HabitacionDAO {

    @Override
    public void guardar(Habitacion h) throws PersistenciaException {
        String sql = "INSERT INTO habitacion (numero, piso, tipo, capacidad) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, h.getNumero());
            ps.setInt(2, h.getPiso());
            ps.setString(3, h.getTipo().name());
            ps.setInt(4, h.getCapacidad());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar la habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Habitacion> buscar(int numero) throws PersistenciaException {
        String sql = "SELECT numero, piso, tipo FROM habitacion WHERE numero = ?";
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(convertir(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar la habitación: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Habitacion> listar() throws PersistenciaException {
        return consultar("SELECT numero, piso, tipo FROM habitacion ORDER BY numero", null);
    }

    @Override
    public List<Habitacion> listarPorTipo(TipoHabitacion tipo) throws PersistenciaException {
        return consultar("SELECT numero, piso, tipo FROM habitacion WHERE tipo = ? ORDER BY numero", tipo.name());
    }

    @Override
    public void eliminar(int numero) throws PersistenciaException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement("DELETE FROM habitacion WHERE numero = ?")) {
            ps.setInt(1, numero);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se puede eliminar la habitación " + numero + " porque tiene reservas asociadas.", e);
        }
    }

    private List<Habitacion> consultar(String sql, String parametro) throws PersistenciaException {
        List<Habitacion> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setString(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(convertir(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron cargar las habitaciones: " + e.getMessage(), e);
        }
    }

    /**
     * Reconstrucción con el TIPO CORRECTO: el texto de la columna "tipo" se convierte
     * en TipoHabitacion y este crea la subclase (Sencilla, Doble o Suite). Sin switch.
     */
    static Habitacion convertir(ResultSet rs) throws SQLException, PersistenciaException {
        try {
            return TipoHabitacion.valueOf(rs.getString("tipo")).crear(rs.getInt("numero"), rs.getInt("piso"));
        } catch (HabitacionInvalidaException | IllegalArgumentException e) {
            throw new PersistenciaException("Habitación con datos inválidos en la base de datos: " + e.getMessage(), e);
        }
    }
}
