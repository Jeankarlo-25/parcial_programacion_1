package persistencia;

import excepciones.PersistenciaException;
import java.util.List;
import java.util.Optional;
import modelo.Habitacion;
import modelo.TipoHabitacion;

/** Interfaz de acceso a datos de las habitaciones. */
public interface HabitacionDAO {

    void guardar(Habitacion habitacion) throws PersistenciaException;

    Optional<Habitacion> buscar(int numero) throws PersistenciaException;

    List<Habitacion> listar() throws PersistenciaException;

    List<Habitacion> listarPorTipo(TipoHabitacion tipo) throws PersistenciaException;

    void eliminar(int numero) throws PersistenciaException;
}
