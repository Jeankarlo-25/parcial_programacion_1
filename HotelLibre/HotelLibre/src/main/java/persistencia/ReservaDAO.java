package persistencia;

import excepciones.PersistenciaException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import modelo.Reserva;

/** Interfaz de acceso a datos (DAO) de las reservas. */
public interface ReservaDAO {

    /** Inserta la reserva y devuelve el id generado. */
    int guardar(Reserva reserva) throws PersistenciaException;

    void actualizar(Reserva reserva) throws PersistenciaException;

    void eliminar(int id) throws PersistenciaException;

    List<Reserva> listar() throws PersistenciaException;

    List<Reserva> listarPorHabitacion(int numeroHabitacion) throws PersistenciaException;

    /** Consulta de negocio a): reservas que ocupan una habitación en la fecha dada. */
    List<Reserva> ocupadasEnFecha(LocalDate fecha) throws PersistenciaException;

    /** Consulta de negocio b): reservas que tienen al menos una noche en el mes. */
    List<Reserva> conNochesEnMes(YearMonth mes) throws PersistenciaException;
}
