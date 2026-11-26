package persistencia;

import excepciones.HotelException;
import excepciones.PersistenciaException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import modelo.Habitacion;
import modelo.Reserva;
import modelo.ReservaBuilder;

/** Implementación JDBC (SQLite) de ReservaDAO. Todas las sentencias usan PreparedStatement. */
public class ReservaDAOJdbc implements ReservaDAO {

    /** Une la reserva con su habitación para poder reconstruir la subclase correcta. */
    private static final String SELECT_BASE =
            "SELECT r.id, r.huesped, r.fecha_entrada, r.fecha_salida, r.personas, r.desayuno, "
            + "r.parqueadero, r.salida_tardia, r.consumo_minibar, h.numero, h.piso, h.tipo "
            + "FROM reserva r JOIN habitacion h ON h.numero = r.numero_habitacion ";

    @Override
    public int guardar(Reserva r) throws PersistenciaException {
        String sql = "INSERT INTO reserva (huesped, numero_habitacion, fecha_entrada, fecha_salida, personas, "
                + "desayuno, parqueadero, salida_tardia, consumo_minibar) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            llenarParametros(ps, r);
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                return llaves.next() ? llaves.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar la reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Reserva r) throws PersistenciaException {
        String sql = "UPDATE reserva SET huesped = ?, numero_habitacion = ?, fecha_entrada = ?, fecha_salida = ?, "
                + "personas = ?, desayuno = ?, parqueadero = ?, salida_tardia = ?, consumo_minibar = ? WHERE id = ?";
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            llenarParametros(ps, r);
            ps.setInt(10, r.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar la reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) throws PersistenciaException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement("DELETE FROM reserva WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo eliminar la reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reserva> listar() throws PersistenciaException {
        return consultar(SELECT_BASE + "ORDER BY r.id");
    }

    @Override
    public List<Reserva> listarPorHabitacion(int numeroHabitacion) throws PersistenciaException {
        return consultar(SELECT_BASE + "WHERE r.numero_habitacion = ? ORDER BY r.fecha_entrada",
                numeroHabitacion);
    }

    /** a) Habitaciones ocupadas en una fecha: entrada <= fecha < salida (el día de salida ya está libre). */
    @Override
    public List<Reserva> ocupadasEnFecha(LocalDate fecha) throws PersistenciaException {
        return consultar(SELECT_BASE + "WHERE r.fecha_entrada <= ? AND r.fecha_salida > ? ORDER BY h.numero",
                fecha.toString(), fecha.toString());
    }

    /** b) Reservas con al menos una noche dentro del mes (entrada antes de fin de mes y salida después del día 1). */
    @Override
    public List<Reserva> conNochesEnMes(YearMonth mes) throws PersistenciaException {
        String primerDia = mes.atDay(1).toString();
        String primerDiaMesSiguiente = mes.plusMonths(1).atDay(1).toString();
        return consultar(SELECT_BASE + "WHERE r.fecha_entrada < ? AND r.fecha_salida > ? ORDER BY h.tipo, h.numero",
                primerDiaMesSiguiente, primerDia);
    }

    // ------------------- Apoyo -------------------

    private void llenarParametros(PreparedStatement ps, Reserva r) throws SQLException {
        ps.setString(1, r.getHuesped());
        ps.setInt(2, r.getHabitacion().getNumero());
        ps.setString(3, r.getEntrada().toString());
        ps.setString(4, r.getSalida().toString());
        ps.setInt(5, r.getPersonas());
        ps.setInt(6, r.isDesayuno() ? 1 : 0);
        ps.setInt(7, r.isParqueadero() ? 1 : 0);
        ps.setInt(8, r.isSalidaTardia() ? 1 : 0);
        ps.setDouble(9, r.getConsumoMinibar());
    }

    private List<Reserva> consultar(String sql, Object... parametros) throws PersistenciaException {
        List<Reserva> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(convertir(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron consultar las reservas: " + e.getMessage(), e);
        }
    }

    /**
     * Reconstruye la reserva leída: la habitación con su subclase correcta y la
     * reserva con el mismo ReservaBuilder que se usa al registrar (patrón Builder).
     * Así el cálculo da exactamente el mismo resultado que antes de guardarse.
     */
    private Reserva convertir(ResultSet rs) throws SQLException, PersistenciaException {
        Habitacion habitacion = HabitacionDAOJdbc.convertir(rs);
        ReservaBuilder builder = new ReservaBuilder(rs.getString("huesped"), habitacion,
                LocalDate.parse(rs.getString("fecha_entrada")), LocalDate.parse(rs.getString("fecha_salida")),
                rs.getInt("personas"))
                .conId(rs.getInt("id"))
                .conMinibar(rs.getDouble("consumo_minibar"));
        if (rs.getInt("desayuno") == 1) {
            builder.conDesayuno();
        }
        if (rs.getInt("parqueadero") == 1) {
            builder.conParqueadero();
        }
        if (rs.getInt("salida_tardia") == 1) {
            builder.conSalidaTardia();
        }
        try {
            return builder.build();
        } catch (HotelException e) {
            throw new PersistenciaException("Reserva " + rs.getInt("id") + " con datos inválidos: " + e.getMessage(), e);
        }
    }
}
