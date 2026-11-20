import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class VentanaPaqueteria extends JFrame {
    private static final String[] CIUDADES = {
            "Quito", "Guayaquil", "Cuenca", "Ambato", "Riobamba",
            "Loja", "Ibarra", "Manta", "Machala", "Esmeraldas"
    };
    private static final String[] ESTADOS = {"Receptado", "Enviado", "Recibido"};

    private final Lista paquetes = new Lista();

    private JSpinner spTracking;
    private JTextField txtPeso;
    private JComboBox<String> cbCiudadRecepcion;
    private JComboBox<String> cbCiudadEntrega;
    private JTextField txtCedula;
    private JList<Paqueteria> listaPaquetes;
    private DefaultListModel<Paqueteria> modeloPaquetes;

    private JSpinner spTrackingEstado;
    private JList<Paqueteria> listaEstados;
    private DefaultListModel<Paqueteria> modeloEstados;

    private JComboBox<String> cbCiudadEstadistica;
    private JComboBox<String> cbEstadoEstadistica;

    private DefaultListModel<Paqueteria> modeloOriginal;
    private DefaultListModel<Paqueteria> modeloOrdenado;
    private JLabel lblOrdenado;

    private JTextField txtTrackingBinaria;
    private DefaultListModel<Paqueteria> modeloBinaria;
    private JLabel lblBinaria;
    private JTextField txtCedulaFiltro;
    private JComboBox<String> cbEstadoFiltro;
    private DefaultListModel<Paqueteria> modeloFiltro;

    private JTabbedPane pestanas;

    public VentanaPaqueteria() {
        super("Sistema de Gestión de Paquetería");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pestanas = new JTabbedPane();
        pestanas.addTab("Registro y modificación", crearPanelRegistro());
        pestanas.addTab("Control de estados", crearPanelEstados());
        pestanas.addTab("Estadísticas (recursividad)", crearPanelEstadisticas());
        pestanas.addTab("Ordenamiento", crearPanelOrdenamiento());
        pestanas.addTab("Búsqueda", crearPanelBusqueda());

        pestanas.addChangeListener(e -> refrescarListas());

        setContentPane(pestanas);
        cargarDatosDePrueba();
        refrescarListas();

        setSize(980, 560);
        setLocationRelativeTo(null);
    }

    private JPanel crearPanelRegistro() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(new TitledBorder("Datos del paquete"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        spTracking = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
        txtPeso = new JTextField(10);
        cbCiudadRecepcion = new JComboBox<>(CIUDADES);
        cbCiudadEntrega = new JComboBox<>(CIUDADES);
        txtCedula = new JTextField(10);

        agregarFila(formulario, c, 0, "Tracking:", spTracking);
        agregarFila(formulario, c, 1, "Peso (kg):", txtPeso);
        agregarFila(formulario, c, 2, "Ciudad de recepción:", cbCiudadRecepcion);
        agregarFila(formulario, c, 3, "Ciudad de entrega:", cbCiudadEntrega);
        agregarFila(formulario, c, 4, "Cédula del receptor:", txtCedula);

        JButton btnRegistrar = new JButton("Registrar");
        JButton btnModificar = new JButton("Modificar seleccionado");
        JButton btnLimpiar = new JButton("Limpiar");
        JPanel botones = new JPanel(new GridLayout(3, 1, 5, 5));
        botones.add(btnRegistrar);
        botones.add(btnModificar);
        botones.add(btnLimpiar);
        c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
        formulario.add(botones, c);

        modeloPaquetes = new DefaultListModel<>();
        listaPaquetes = new JList<>(modeloPaquetes);
        listaPaquetes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(listaPaquetes);
        scroll.setBorder(new TitledBorder("Paquetes registrados (seleccione uno para modificarlo)"));

        btnRegistrar.addActionListener(e -> registrarPaquete());
        btnModificar.addActionListener(e -> modificarPaquete());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        listaPaquetes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.add(formulario, BorderLayout.NORTH);
        panel.add(izquierda, BorderLayout.WEST);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelEstados() {
        spTrackingEstado = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
        JButton btnActualizar = new JButton("Actualizar estado");

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        arriba.setBorder(new TitledBorder("Secuencia: Receptado → Enviado → Recibido"));
        arriba.add(new JLabel("Tracking:"));
        arriba.add(spTrackingEstado);
        arriba.add(btnActualizar);

        modeloEstados = new DefaultListModel<>();
        listaEstados = new JList<>(modeloEstados);
        listaEstados.addListSelectionListener(e -> {
            Paqueteria p = listaEstados.getSelectedValue();
            if (!e.getValueIsAdjusting() && p != null) {
                spTrackingEstado.setValue(p.getTracking());
            }
        });
        JScrollPane scroll = new JScrollPane(listaEstados);
        scroll.setBorder(new TitledBorder("Paquetes y su estado actual"));

        btnActualizar.addActionListener(e -> actualizarEstado());

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(arriba, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        JButton btnTotal = new JButton("Número total de paquetes");
        JButton btnPeso = new JButton("Peso acumulado de todos los paquetes");
        cbCiudadEstadistica = new JComboBox<>(CIUDADES);
        JButton btnPesoCiudad = new JButton("Peso total recibido en la ciudad");
        cbEstadoEstadistica = new JComboBox<>(ESTADOS);
        JButton btnPorEstado = new JButton("Cantidad de paquetes en el estado");

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(btnTotal, c);
        c.gridy = 1;
        panel.add(btnPeso, c);
        c.gridwidth = 1;
        c.gridy = 2; c.gridx = 0;
        panel.add(cbCiudadEstadistica, c);
        c.gridx = 1;
        panel.add(btnPesoCiudad, c);
        c.gridy = 3; c.gridx = 0;
        panel.add(cbEstadoEstadistica, c);
        c.gridx = 1;
        panel.add(btnPorEstado, c);

        btnTotal.addActionListener(e -> mostrarMensaje(
                "Número total de paquetes registrados: " + paquetes.sumarTotalPaquetes()));
        btnPeso.addActionListener(e -> mostrarMensaje(
                "Peso acumulado de todos los paquetes: " + paquetes.sumarTotalPeso() + " kg"));
        btnPesoCiudad.addActionListener(e -> {
            String ciudad = (String) cbCiudadEstadistica.getSelectedItem();
            mostrarMensaje("Peso total de los paquetes recibidos en " + ciudad + ": "
                    + paquetes.sumarTotalPesoCiudad(ciudad) + " kg");
        });
        btnPorEstado.addActionListener(e -> {
            String estado = (String) cbEstadoEstadistica.getSelectedItem();
            mostrarMensaje("Paquetes en estado \"" + estado + "\": "
                    + paquetes.sumarTotalPaquetesEstado(estado));
        });

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(new JLabel("  Todos los cálculos de esta pestaña se realizan con métodos recursivos."),
                BorderLayout.NORTH);
        contenedor.add(panel, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel crearPanelOrdenamiento() {
        JButton btnBurbuja = new JButton("Burbuja (por tracking)");
        JButton btnInsercion = new JButton("Inserción (por peso)");
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        arriba.add(btnBurbuja);
        arriba.add(btnInsercion);

        modeloOriginal = new DefaultListModel<>();
        modeloOrdenado = new DefaultListModel<>();
        JScrollPane scrollOriginal = new JScrollPane(new JList<>(modeloOriginal));
        scrollOriginal.setBorder(new TitledBorder("Orden original (no se modifica)"));
        JPanel derecha = new JPanel(new BorderLayout());
        lblOrdenado = new JLabel(" Presione un botón para ordenar");
        derecha.add(lblOrdenado, BorderLayout.NORTH);
        derecha.add(new JScrollPane(new JList<>(modeloOrdenado)), BorderLayout.CENTER);
        derecha.setBorder(new TitledBorder("Lista ordenada"));

        JPanel centro = new JPanel(new GridLayout(2, 1, 10, 10));
        centro.add(scrollOriginal);
        centro.add(derecha);

        btnBurbuja.addActionListener(e -> {
            llenarModelo(modeloOrdenado, paquetes.ordenarPaquetesPorTracking());
            lblOrdenado.setText(" Ordenado por tracking (ascendente) - método burbuja");
            llenarModelo(modeloOriginal, paquetes.getServiEntrega());
        });
        btnInsercion.addActionListener(e -> {
            llenarModelo(modeloOrdenado, paquetes.ordenarPaquetesPorPeso());
            lblOrdenado.setText(" Ordenado por peso (ascendente) - método inserción");
            llenarModelo(modeloOriginal, paquetes.getServiEntrega());
        });

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(arriba, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelBusqueda() {
        txtTrackingBinaria = new JTextField(8);
        JButton btnBinaria = new JButton("Buscar (binaria)");
        modeloBinaria = new DefaultListModel<>();
        lblBinaria = new JLabel(" ");
        JPanel filaBinaria = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaBinaria.add(new JLabel("Tracking:"));
        filaBinaria.add(txtTrackingBinaria);
        filaBinaria.add(btnBinaria);
        filaBinaria.add(lblBinaria);
        JPanel panelBinaria = new JPanel(new BorderLayout());
        panelBinaria.setBorder(new TitledBorder("Búsqueda binaria por tracking"));
        panelBinaria.add(filaBinaria, BorderLayout.NORTH);
        panelBinaria.add(new JScrollPane(new JList<>(modeloBinaria)), BorderLayout.CENTER);

        txtCedulaFiltro = new JTextField(10);
        cbEstadoFiltro = new JComboBox<>(ESTADOS);
        JButton btnFiltrar = new JButton("Filtrar");
        modeloFiltro = new DefaultListModel<>();
        JPanel filaFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaFiltro.add(new JLabel("Cédula del receptor:"));
        filaFiltro.add(txtCedulaFiltro);
        filaFiltro.add(new JLabel("Estado:"));
        filaFiltro.add(cbEstadoFiltro);
        filaFiltro.add(btnFiltrar);
        JPanel panelFiltro = new JPanel(new BorderLayout());
        panelFiltro.setBorder(new TitledBorder("Búsqueda por cédula del receptor y estado"));
        panelFiltro.add(filaFiltro, BorderLayout.NORTH);
        panelFiltro.add(new JScrollPane(new JList<>(modeloFiltro)), BorderLayout.CENTER);

        btnBinaria.addActionListener(e -> busquedaBinaria());
        btnFiltrar.addActionListener(e -> filtrarPorCedulaYEstado());

        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(panelBinaria);
        panel.add(panelFiltro);
        return panel;
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, String texto, JComponent campo) {
        c.gridwidth = 1;
        c.gridx = 0; c.gridy = fila; c.weightx = 0;
        panel.add(new JLabel(texto), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(campo, c);
    }

    private void registrarPaquete() {
        try {
            int tracking = (Integer) spTracking.getValue();
            double peso = leerPeso();
            String cedula = leerCedula();
            Paqueteria p = new Paqueteria(tracking, peso,
                    (String) cbCiudadRecepcion.getSelectedItem(),
                    (String) cbCiudadEntrega.getSelectedItem(),
                    cedula);
            paquetes.adicionarElementos(p);
            refrescarListas();
            mostrarMensaje("Paquete " + tracking + " registrado correctamente.");
            limpiarFormulario();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void modificarPaquete() {
        Paqueteria seleccionado = listaPaquetes.getSelectedValue();
        if (seleccionado == null) {
            mostrarError("Seleccione un paquete de la lista para modificarlo.");
            return;
        }
        try {
            double peso = leerPeso();
            String cedula = leerCedula();
            paquetes.editar(seleccionado.getTracking(), peso,
                    (String) cbCiudadRecepcion.getSelectedItem(),
                    (String) cbCiudadEntrega.getSelectedItem(),
                    cedula);
            refrescarListas();
            mostrarMensaje("Paquete " + seleccionado.getTracking() + " modificado correctamente.");
            limpiarFormulario();
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarSeleccionEnFormulario() {
        Paqueteria p = listaPaquetes.getSelectedValue();
        if (p == null) {
            return;
        }
        spTracking.setValue(p.getTracking());
        spTracking.setEnabled(false);
        txtPeso.setText(String.valueOf(p.getPeso()));
        cbCiudadRecepcion.setSelectedItem(p.getCiudadRecepcion());
        cbCiudadEntrega.setSelectedItem(p.getCiudadEntrega());
        txtCedula.setText(p.getCedulaReceptor());
    }

    private void limpiarFormulario() {
        listaPaquetes.clearSelection();
        spTracking.setEnabled(true);
        spTracking.setValue(siguienteTracking());
        txtPeso.setText("");
        cbCiudadRecepcion.setSelectedIndex(0);
        cbCiudadEntrega.setSelectedIndex(0);
        txtCedula.setText("");
    }

    private void actualizarEstado() {
        int tracking = (Integer) spTrackingEstado.getValue();
        try {
            String resultado = paquetes.estadoPaquete(tracking);
            if (resultado.equals("Finalizado")) {
                mostrarMensaje("El paquete " + tracking
                        + " ya fue recibido. El proceso de envío ha finalizado.");
            } else {
                mostrarMensaje("El paquete " + tracking + " cambió al estado: " + resultado);
            }
            refrescarListas();
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void busquedaBinaria() {
        modeloBinaria.clear();
        try {
            int tracking = Integer.parseInt(txtTrackingBinaria.getText().trim());
            List<Paqueteria> ordenada = paquetes.ordenarPaquetesPorTracking();
            int posicion = paquetes.busquedaBinaria(ordenada, tracking);
            if (posicion == -1) {
                lblBinaria.setText("Resultado: -1 (no existe)");
                mostrarError("No existe un paquete con el tracking " + tracking + ".");
            } else {
                modeloBinaria.addElement(ordenada.get(posicion));
                lblBinaria.setText("Encontrado en la posición " + posicion + " de la lista ordenada");
            }
        } catch (NumberFormatException ex) {
            mostrarError("Ingrese un número de tracking válido.");
        }
    }

    private void filtrarPorCedulaYEstado() {
        String cedula = txtCedulaFiltro.getText().trim();
        if (cedula.isEmpty()) {
            mostrarError("Ingrese la cédula del receptor.");
            return;
        }
        String estado = (String) cbEstadoFiltro.getSelectedItem();
        List<Paqueteria> resultado = paquetes.buscarPaquetesPorCedulaYEstado(cedula, estado);
        llenarModelo(modeloFiltro, resultado);
        if (resultado.isEmpty()) {
            mostrarMensaje("No hay paquetes con cédula " + cedula + " en estado \"" + estado + "\".");
        }
    }

    private double leerPeso() throws Exception {
        String texto = txtPeso.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            throw new Exception("Ingrese el peso del paquete.");
        }
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException ex) {
            throw new Exception("El peso debe ser un número (por ejemplo 12.5).");
        }
    }

    private String leerCedula() throws Exception {
        String cedula = txtCedula.getText().trim();
        if (!cedula.matches("\\d{10}")) {
            throw new Exception("La cédula del receptor debe tener 10 dígitos numéricos.");
        }
        return cedula;
    }

    private int siguienteTracking() {
        int max = 0;
        for (Paqueteria p : paquetes.getServiEntrega()) {
            if (p.getTracking() > max) {
                max = p.getTracking();
            }
        }
        return max + 1;
    }

    private void llenarModelo(DefaultListModel<Paqueteria> modelo, List<Paqueteria> datos) {
        modelo.clear();
        for (Paqueteria p : datos) {
            modelo.addElement(p);
        }
    }

    private void refrescarListas() {
        Paqueteria seleccionado = listaPaquetes.getSelectedValue();
        llenarModelo(modeloPaquetes, paquetes.getServiEntrega());
        if (seleccionado != null) {
            listaPaquetes.setSelectedValue(seleccionado, true);
        }
        llenarModelo(modeloEstados, paquetes.getServiEntrega());
        llenarModelo(modeloOriginal, paquetes.getServiEntrega());
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void cargarDatosDePrueba() {
        try {
            paquetes.adicionarElementos(new Paqueteria(105, 25.5, "Quito", "Guayaquil", "1712345678"));
            paquetes.adicionarElementos(new Paqueteria(102, 12.0, "Cuenca", "Riobamba", "0102030405"));
            paquetes.adicionarElementos(new Paqueteria(108, 40.2, "Quito", "Ibarra", "1712345678"));
            paquetes.adicionarElementos(new Paqueteria(101, 8.75, "Guayaquil", "Manta", "0911223344"));
            paquetes.adicionarElementos(new Paqueteria(110, 18.3, "Quito", "Loja", "1712345678"));

            paquetes.estadoPaquete(108);
            paquetes.estadoPaquete(110);
            paquetes.estadoPaquete(110);
        } catch (Exception ex) {
            mostrarError("Error al cargar los datos de prueba: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPaqueteria().setVisible(true));
    }
}
