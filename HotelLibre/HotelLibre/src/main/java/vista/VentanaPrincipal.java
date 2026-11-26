package vista;

import excepciones.DatosInvalidosException;
import excepciones.HotelException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import modelo.Habitacion;
import modelo.Reserva;
import modelo.ReservaBuilder;
import modelo.TipoHabitacion;
import servicio.HotelService;

/**
 * Ventana principal de HotelLibre.
 * Solo muestra y captura datos: las reglas están en el modelo (ReservaBuilder,
 * Habitacion...) y el acceso a datos en los DAO, a través de HotelService.
 * Toda excepción se muestra como mensaje y la ventana NO se cierra.
 */
public class VentanaPrincipal extends JFrame {

    /** Acción de la ventana que puede lanzar una excepción del negocio. */
    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws HotelException;
    }

    private final HotelService servicio;

    // ---------- Pestaña Reservas ----------
    private final DefaultTableModel modeloReservas = UtilVista.modeloTabla("ID", "Huésped", "Hab.", "Tipo",
            "Entrada", "Salida", "Noches", "Pers.", "Servicios", "Alojamiento", "Opcionales", "Minibar (+IVA)", "Total");
    private final JTable tablaReservas = new JTable(modeloReservas);
    private List<Reserva> reservasMostradas = List.of();
    private int idSeleccionado = 0;

    private final JComboBox<TipoHabitacion> cmbTipo = new JComboBox<>(TipoHabitacion.values());
    private final JComboBox<Habitacion> cmbHabitacion = new JComboBox<>();
    private final JLabel lblCapacidad = new JLabel("-");
    private final JTextField txtHuesped = new JTextField();
    private final JTextField txtEntrada = new JTextField();
    private final JTextField txtSalida = new JTextField();
    private final JSpinner spnPersonas = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private final JCheckBox chkDesayuno = new JCheckBox("Desayuno ($25.000/persona/noche)");
    private final JCheckBox chkParqueadero = new JCheckBox("Parqueadero ($15.000/noche)");
    private final JCheckBox chkSalidaTardia = new JCheckBox("Salida tardía ($50.000)");
    private final JTextField txtMinibar = new JTextField("0");
    private final JLabel lblModo = new JLabel("Nueva reserva");

    // ---------- Pestaña Habitaciones ----------
    private final DefaultTableModel modeloHabitaciones =
            UtilVista.modeloTabla("Número", "Piso", "Tipo", "Capacidad", "Minibar");
    private final JTable tablaHabitaciones = new JTable(modeloHabitaciones);
    private final JComboBox<TipoHabitacion> cmbTipoNueva = new JComboBox<>(TipoHabitacion.values());
    private final JTextField txtNumeroNueva = new JTextField();
    private final JTextField txtPisoNueva = new JTextField();
    private List<Habitacion> habitacionesMostradas = List.of();

    // ---------- Pestaña Consultas ----------
    private final DefaultTableModel modeloOcupadas =
            UtilVista.modeloTabla("Habitación", "Tipo", "Huésped", "Entrada", "Salida", "Personas");
    private final DefaultTableModel modeloIngresos = UtilVista.modeloTabla("Tipo de habitación", "Ingresos de alojamiento");
    private final JTextField txtFechaConsulta = new JTextField(LocalDate.now().toString(), 10);
    private final JTextField txtMesConsulta = new JTextField(YearMonth.now().toString(), 8);

    // ---------- Barra de mensajes (dentro de la ventana) ----------
    private final JLabel lblMensaje = new JLabel(" ");

    public VentanaPrincipal(HotelService servicio) {
        super("HotelLibre - Gestión de reservas (Variante 4)");
        this.servicio = servicio;

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Reservas", panelReservas());
        pestanas.addTab("Habitaciones", panelHabitaciones());
        pestanas.addTab("Consultas", panelConsultas());
        pestanas.addChangeListener(e -> refrescarTodo());

        lblMensaje.setBorder(new EmptyBorder(6, 10, 6, 10));
        lblMensaje.setFont(lblMensaje.getFont().deriveFont(Font.BOLD));

        add(pestanas, BorderLayout.CENTER);
        add(lblMensaje, BorderLayout.SOUTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1500, 640);
        setLocationRelativeTo(null);

        cargarHabitacionesDelTipo();
        refrescarTodo();
    }

    // =====================================================================
    // Pestaña RESERVAS (R5 ver, R6 guardar, R7 editar, R8 eliminar)
    // =====================================================================

    private JPanel panelReservas() {
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaReservas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarReservaEnFormulario();
            }
        });
        int[] anchos = {30, 120, 40, 65, 85, 85, 50, 40, 170, 100, 90, 100, 105};
        for (int i = 0; i < anchos.length; i++) {
            tablaReservas.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        // El tipo elegido filtra las habitaciones; la habitación elegida habilita o no el minibar.
        cmbTipo.addActionListener(e -> cargarHabitacionesDelTipo());
        cmbHabitacion.addActionListener(e -> actualizarCamposSegunHabitacion());

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Tipo de habitación:"));
        form.add(cmbTipo);
        form.add(new JLabel("Habitación:"));
        form.add(cmbHabitacion);
        form.add(new JLabel("Capacidad máxima:"));
        form.add(lblCapacidad);
        form.add(new JLabel("Huésped:"));
        form.add(txtHuesped);
        form.add(new JLabel("Fecha entrada (aaaa-mm-dd):"));
        form.add(txtEntrada);
        form.add(new JLabel("Fecha salida (aaaa-mm-dd):"));
        form.add(txtSalida);
        form.add(new JLabel("Número de personas:"));
        form.add(spnPersonas);
        form.add(new JLabel("Opcionales:"));
        form.add(chkDesayuno);
        form.add(new JLabel(""));
        form.add(chkParqueadero);
        form.add(new JLabel(""));
        form.add(chkSalidaTardia);
        form.add(new JLabel("Consumo minibar (doble/suite):"));
        form.add(txtMinibar);

        JButton btnGuardar = new JButton("Guardar nueva");
        JButton btnEditar = new JButton("Guardar cambios");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        btnGuardar.addActionListener(e -> ejecutar(this::guardarReserva));
        btnEditar.addActionListener(e -> ejecutar(this::editarReserva));
        btnEliminar.addActionListener(e -> ejecutar(this::eliminarReserva));
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        JPanel botones = new JPanel(new GridLayout(2, 2, 6, 6));
        botones.add(btnGuardar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        lblModo.setFont(lblModo.getFont().deriveFont(Font.BOLD, 14f));
        JPanel izquierda = new JPanel(new BorderLayout(6, 10));
        izquierda.setBorder(new EmptyBorder(10, 10, 10, 10));
        izquierda.add(lblModo, BorderLayout.NORTH);
        izquierda.add(form, BorderLayout.CENTER);
        izquierda.add(botones, BorderLayout.SOUTH);
        izquierda.setPreferredSize(new Dimension(440, izquierda.getPreferredSize().height));

        JPanel envoltura = new JPanel(new BorderLayout()); // evita que el formulario se estire
        envoltura.add(izquierda, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(envoltura, BorderLayout.WEST);
        panel.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);
        return panel;
    }

    /** Arma el ReservaBuilder con lo que escribió el usuario (obligatorios + opcionales elegidos). */
    private ReservaBuilder leerFormulario() throws HotelException {
        LocalDate entrada = UtilVista.leerFecha(txtEntrada.getText(), "fecha de entrada");
        LocalDate salida = UtilVista.leerFecha(txtSalida.getText(), "fecha de salida");
        ReservaBuilder builder = new ReservaBuilder(txtHuesped.getText(),
                (Habitacion) cmbHabitacion.getSelectedItem(), entrada, salida, (Integer) spnPersonas.getValue());
        if (chkDesayuno.isSelected()) {
            builder.conDesayuno();
        }
        if (chkParqueadero.isSelected()) {
            builder.conParqueadero();
        }
        if (chkSalidaTardia.isSelected()) {
            builder.conSalidaTardia();
        }
        if (txtMinibar.isEnabled()) {
            builder.conMinibar(UtilVista.leerDinero(txtMinibar.getText(), "consumo de minibar"));
        }
        return builder;
    }

    private void guardarReserva() throws HotelException {
        Reserva r = servicio.registrarReserva(leerFormulario());
        refrescarReservas();
        limpiarFormulario();
        exito("Reserva #" + r.getId() + " guardada. Habitación " + r.getHabitacion().getNumero()
                + " - Total: " + UtilVista.pesos(r.getTotal()));
    }

    private void editarReserva() throws HotelException {
        Reserva r = servicio.actualizarReserva(idSeleccionado, leerFormulario());
        refrescarReservas();
        limpiarFormulario();
        exito("Reserva #" + r.getId() + " actualizada. Nuevo total: " + UtilVista.pesos(r.getTotal()));
    }

    private void eliminarReserva() throws HotelException {
        if (idSeleccionado <= 0) {
            servicio.eliminarReserva(0); // lanza el mensaje "Seleccione en la tabla..."
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea eliminar la reserva #" + idSeleccionado + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            int id = idSeleccionado;
            servicio.eliminarReserva(id);
            refrescarReservas();
            limpiarFormulario();
            exito("Reserva #" + id + " eliminada.");
        }
    }

    private void cargarReservaEnFormulario() {
        int fila = tablaReservas.getSelectedRow();
        if (fila < 0 || fila >= reservasMostradas.size()) {
            return;
        }
        Reserva r = reservasMostradas.get(fila);
        idSeleccionado = r.getId();
        cmbTipo.setSelectedItem(r.getHabitacion().getTipo()); // recarga las habitaciones del tipo
        seleccionarHabitacion(r.getHabitacion().getNumero());
        txtHuesped.setText(r.getHuesped());
        txtEntrada.setText(r.getEntrada().toString());
        txtSalida.setText(r.getSalida().toString());
        spnPersonas.setValue(r.getPersonas());
        chkDesayuno.setSelected(r.isDesayuno());
        chkParqueadero.setSelected(r.isParqueadero());
        chkSalidaTardia.setSelected(r.isSalidaTardia());
        txtMinibar.setText(String.valueOf(Math.round(r.getConsumoMinibar())));
        lblModo.setText("Editando reserva #" + r.getId() + " (modifique y pulse \"Guardar cambios\")");
    }

    private void seleccionarHabitacion(int numero) {
        for (int i = 0; i < cmbHabitacion.getItemCount(); i++) {
            if (cmbHabitacion.getItemAt(i).getNumero() == numero) {
                cmbHabitacion.setSelectedIndex(i);
                return;
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = 0;
        tablaReservas.clearSelection();
        txtHuesped.setText("");
        txtEntrada.setText("");
        txtSalida.setText("");
        spnPersonas.setValue(1);
        chkDesayuno.setSelected(false);
        chkParqueadero.setSelected(false);
        chkSalidaTardia.setSelected(false);
        txtMinibar.setText("0");
        lblModo.setText("Nueva reserva");
    }

    /** R6: al cambiar el tipo se cargan sus habitaciones. */
    private void cargarHabitacionesDelTipo() {
        ejecutar(() -> {
            Habitacion anterior = (Habitacion) cmbHabitacion.getSelectedItem();
            cmbHabitacion.removeAllItems();
            for (Habitacion h : servicio.listarHabitaciones((TipoHabitacion) cmbTipo.getSelectedItem())) {
                cmbHabitacion.addItem(h);
            }
            if (anterior != null) { // conserva la habitación elegida si sigue en la lista
                seleccionarHabitacion(anterior.getNumero());
            }
            actualizarCamposSegunHabitacion();
        });
    }

    /** R6: el campo de minibar solo se habilita si la habitación tiene minibar (doble y suite). */
    private void actualizarCamposSegunHabitacion() {
        Habitacion h = (Habitacion) cmbHabitacion.getSelectedItem();
        boolean tieneMinibar = h != null && h.getMinibar().isPresent();
        txtMinibar.setEnabled(tieneMinibar);
        if (!tieneMinibar) {
            txtMinibar.setText("0");
        }
        lblCapacidad.setText(h == null ? "-" : h.getCapacidad() + " persona(s)");
    }

    private void refrescarReservas() throws HotelException {
        reservasMostradas = servicio.listarReservas();
        modeloReservas.setRowCount(0);
        for (Reserva r : reservasMostradas) {
            modeloReservas.addRow(new Object[]{
                r.getId(), r.getHuesped(), r.getHabitacion().getNumero(), r.getHabitacion().getTipo(),
                r.getEntrada(), r.getSalida(), r.getNoches(), r.getPersonas(), r.getServiciosTexto(),
                UtilVista.pesos(r.getAlojamiento()), UtilVista.pesos(r.getOpcionales()),
                UtilVista.pesos(r.getMinibar()), UtilVista.pesos(r.getTotal())});
        }
    }

    // =====================================================================
    // Pestaña HABITACIONES
    // =====================================================================

    private JPanel panelHabitaciones() {
        tablaHabitaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Tipo:"));
        form.add(cmbTipoNueva);
        form.add(new JLabel("Número (3 dígitos):"));
        form.add(txtNumeroNueva);
        form.add(new JLabel("Piso:"));
        form.add(txtPisoNueva);

        JButton btnRegistrar = new JButton("Registrar habitación");
        JButton btnEliminar = new JButton("Eliminar seleccionada");
        btnRegistrar.addActionListener(e -> ejecutar(this::registrarHabitacion));
        btnEliminar.addActionListener(e -> ejecutar(this::eliminarHabitacion));
        JPanel botones = new JPanel(new GridLayout(1, 2, 6, 6));
        botones.add(btnRegistrar);
        botones.add(btnEliminar);

        JPanel izquierda = new JPanel(new BorderLayout(6, 10));
        izquierda.setBorder(new EmptyBorder(10, 10, 10, 10));
        izquierda.add(new JLabel("<html><b>Nueva habitación</b><br>Capacidad: sencilla 1, doble 3, suite 4."
                + "<br>El primer dígito del número es el piso.</html>"), BorderLayout.NORTH);
        izquierda.add(form, BorderLayout.CENTER);
        izquierda.add(botones, BorderLayout.SOUTH);
        izquierda.setPreferredSize(new Dimension(420, izquierda.getPreferredSize().height));

        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.add(izquierda, BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(envoltura, BorderLayout.WEST);
        panel.add(new JScrollPane(tablaHabitaciones), BorderLayout.CENTER);
        return panel;
    }

    private void registrarHabitacion() throws HotelException {
        Habitacion h = servicio.registrarHabitacion((TipoHabitacion) cmbTipoNueva.getSelectedItem(),
                UtilVista.leerEntero(txtNumeroNueva.getText(), "número"),
                UtilVista.leerEntero(txtPisoNueva.getText(), "piso"));
        txtNumeroNueva.setText("");
        txtPisoNueva.setText("");
        refrescarHabitaciones();
        cargarHabitacionesDelTipo();
        exito("Habitación " + h + " registrada.");
    }

    private void eliminarHabitacion() throws HotelException {
        int fila = tablaHabitaciones.getSelectedRow();
        if (fila < 0) {
            throw new DatosInvalidosException("Seleccione en la tabla la habitación a eliminar.");
        }
        int numero = habitacionesMostradas.get(fila).getNumero();
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar la habitación " + numero + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            servicio.eliminarHabitacion(numero);
            refrescarHabitaciones();
            cargarHabitacionesDelTipo();
            exito("Habitación " + numero + " eliminada.");
        }
    }

    private void refrescarHabitaciones() throws HotelException {
        habitacionesMostradas = servicio.listarHabitaciones();
        modeloHabitaciones.setRowCount(0);
        for (Habitacion h : habitacionesMostradas) {
            modeloHabitaciones.addRow(new Object[]{h.getNumero(), h.getPiso(), h.getTipo(), h.getCapacidad(),
                h.getMinibar().isPresent() ? "Sí (IVA 19 %)" : "No"});
        }
    }

    // =====================================================================
    // Pestaña CONSULTAS (R9)
    // =====================================================================

    private JPanel panelConsultas() {
        JButton btnOcupadas = new JButton("Consultar");
        btnOcupadas.addActionListener(e -> ejecutar(this::consultarOcupadas));
        JPanel filtroA = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtroA.add(new JLabel("Fecha (aaaa-mm-dd):"));
        filtroA.add(txtFechaConsulta);
        filtroA.add(btnOcupadas);
        JPanel consultaA = new JPanel(new BorderLayout());
        consultaA.setBorder(BorderFactory.createTitledBorder("a) Habitaciones ocupadas en una fecha, con su huésped"));
        consultaA.add(filtroA, BorderLayout.NORTH);
        consultaA.add(new JScrollPane(new JTable(modeloOcupadas)), BorderLayout.CENTER);

        JButton btnIngresos = new JButton("Consultar");
        btnIngresos.addActionListener(e -> ejecutar(this::consultarIngresos));
        JPanel filtroB = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtroB.add(new JLabel("Mes (aaaa-mm):"));
        filtroB.add(txtMesConsulta);
        filtroB.add(btnIngresos);
        filtroB.add(new JLabel("  (se cuentan solo las noches que caen dentro del mes)"));
        JPanel consultaB = new JPanel(new BorderLayout());
        consultaB.setBorder(BorderFactory.createTitledBorder("b) Ingresos de alojamiento por tipo de habitación en un mes"));
        consultaB.add(filtroB, BorderLayout.NORTH);
        consultaB.add(new JScrollPane(new JTable(modeloIngresos)), BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(consultaA);
        panel.add(consultaB);
        return panel;
    }

    private void consultarOcupadas() throws HotelException {
        LocalDate fecha = UtilVista.leerFecha(txtFechaConsulta.getText(), "fecha de consulta");
        List<Reserva> ocupadas = servicio.habitacionesOcupadas(fecha);
        modeloOcupadas.setRowCount(0);
        for (Reserva r : ocupadas) {
            modeloOcupadas.addRow(new Object[]{r.getHabitacion().getNumero(), r.getHabitacion().getTipo(),
                r.getHuesped(), r.getEntrada(), r.getSalida(), r.getPersonas()});
        }
        exito(ocupadas.size() + " habitación(es) ocupada(s) el " + fecha + ".");
    }

    private void consultarIngresos() throws HotelException {
        YearMonth mes = UtilVista.leerMes(txtMesConsulta.getText());
        Map<TipoHabitacion, Double> ingresos = servicio.ingresosPorTipo(mes);
        modeloIngresos.setRowCount(0);
        double total = 0;
        for (Map.Entry<TipoHabitacion, Double> fila : ingresos.entrySet()) {
            modeloIngresos.addRow(new Object[]{fila.getKey(), UtilVista.pesos(fila.getValue())});
            total += fila.getValue();
        }
        modeloIngresos.addRow(new Object[]{"TOTAL", UtilVista.pesos(total)});
        exito("Ingresos de alojamiento de " + mes + ": " + UtilVista.pesos(total));
    }

    // =====================================================================
    // Manejo de errores y mensajes
    // =====================================================================

    private void refrescarTodo() {
        ejecutar(() -> {
            refrescarReservas();
            refrescarHabitaciones();
        });
    }

    /** Ejecuta una acción; si falla, muestra el mensaje y la ventana sigue abierta. */
    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (HotelException e) {
            error(e.getMessage());
        } catch (RuntimeException e) {
            error("Error inesperado: " + e.getMessage());
        }
    }

    private void exito(String mensaje) {
        lblMensaje.setForeground(new Color(0, 120, 0));
        lblMensaje.setText("✔ " + mensaje);
    }

    private void error(String mensaje) {
        lblMensaje.setForeground(new Color(180, 0, 0));
        lblMensaje.setText("✖ " + mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar la operación", JOptionPane.WARNING_MESSAGE);
    }
}
