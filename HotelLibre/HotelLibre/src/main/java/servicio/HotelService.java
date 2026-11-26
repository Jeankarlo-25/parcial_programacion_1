package servicio;

import excepciones.DatosInvalidosException;
import excepciones.HabitacionInvalidaException;
import excepciones.HotelException;
import excepciones.PersistenciaException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Habitacion;
import modelo.Reserva;
import modelo.ReservaBuilder;
import modelo.TipoHabitacion;
import persistencia.HabitacionDAO;
import persistencia.ReservaDAO;

/**
 * Coordina el modelo y el acceso a datos. La ventana solo habla con esta clase,
 * así no contiene reglas de negocio ni SQL.
 * Recibe las INTERFACES de los DAO (no las clases JDBC).
 */
public class HotelService {

    private final HabitacionDAO habitacionDAO;
    private final ReservaDAO reservaDAO;

    public HotelService(HabitacionDAO habitacionDAO, ReservaDAO reservaDAO) {
        this.habitacionDAO = habitacionDAO;
        this.reservaDAO = reservaDAO;
    }

    // ------------------- Habitaciones -------------------

    public List<Habitacion> listarHabitaciones() throws PersistenciaException {
        return habitacionDAO.listar();
    }

    public List<Habitacion> listarHabitaciones(TipoHabitacion tipo) throws PersistenciaException {
        return habitacionDAO.listarPorTipo(tipo);
    }

    public Habitacion registrarHabitacion(TipoHabitacion tipo, int numero, int piso) throws HotelException {
        if (tipo == null) {
            throw new DatosInvalidosException("Seleccione el tipo de habitación.");
        }
        Habitacion nueva = tipo.crear(numero, piso); // valida número y piso
        if (habitacionDAO.buscar(numero).isPresent()) {
            throw new HabitacionInvalidaException("La habitación " + numero + " ya está registrada.");
        }
        habitacionDAO.guardar(nueva);
        return nueva;
    }

    public void eliminarHabitacion(int numero) throws PersistenciaException {
        habitacionDAO.eliminar(numero);
    }

    // ------------------- Reservas -------------------

    public List<Reserva> listarReservas() throws PersistenciaException {
        return reservaDAO.listar();
    }

    /** Termina de construir la reserva con el Builder (que valida todo) y la guarda. */
    public Reserva registrarReserva(ReservaBuilder builder) throws HotelException {
        Reserva reserva = construir(builder);
        int id = reservaDAO.guardar(reserva);
        return builder.conId(id).build();
    }

    /** Edita una reserva existente: mismas validaciones, sin compararla consigo misma. */
    public Reserva actualizarReserva(int id, ReservaBuilder builder) throws HotelException {
        if (id <= 0) {
            throw new DatosInvalidosException("Seleccione en la tabla la reserva que desea editar.");
        }
        Reserva reserva = construir(builder.conId(id));
        reservaDAO.actualizar(reserva);
        return reserva;
    }

    public void eliminarReserva(int id) throws HotelException {
        if (id <= 0) {
            throw new DatosInvalidosException("Seleccione en la tabla la reserva que desea eliminar.");
        }
        reservaDAO.eliminar(id);
    }

    private Reserva construir(ReservaBuilder builder) throws HotelException {
        Habitacion habitacion = builder.build().getHabitacion(); // primera validación (datos básicos)
        List<Reserva> existentes = reservaDAO.listarPorHabitacion(habitacion.getNumero());
        return builder.verificandoContra(existentes).build();    // validación completa (cruce de fechas)
    }

    // ------------------- Consultas de negocio (R4 / R9) -------------------

    /** a) Habitaciones ocupadas en una fecha, con su huésped. */
    public List<Reserva> habitacionesOcupadas(LocalDate fecha) throws HotelException {
        if (fecha == null) {
            throw new DatosInvalidosException("Indique la fecha a consultar.");
        }
        return reservaDAO.ocupadasEnFecha(fecha);
    }

    /** b) Ingresos de alojamiento por tipo de habitación en un mes (solo las noches de ese mes). */
    public Map<TipoHabitacion, Double> ingresosPorTipo(YearMonth mes) throws HotelException {
        if (mes == null) {
            throw new DatosInvalidosException("Indique el mes a consultar.");
        }
        Map<TipoHabitacion, Double> ingresos = new LinkedHashMap<>();
        for (TipoHabitacion tipo : TipoHabitacion.values()) {
            ingresos.put(tipo, 0.0);
        }
        for (Reserva r : reservaDAO.conNochesEnMes(mes)) {
            ingresos.merge(r.getHabitacion().getTipo(), r.getAlojamientoEnMes(mes), Double::sum);
        }
        return ingresos;
    }
}
