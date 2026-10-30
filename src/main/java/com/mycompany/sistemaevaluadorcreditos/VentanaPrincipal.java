package com.mycompany.sistemaevaluadorcreditos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana principal del Sistema Evaluador de Créditos.
 *
 * Estructura:
 *  - JMenuBar (Archivo / Administración)
 *  - JToolBar (Guardar | Refrescar | Usuarios)
 *  - JTabbedPane (Nuevo análisis / Base de datos)
 *  - JProgressBar en la parte inferior
 *
 * Menú, barra de herramientas y botón del formulario comparten las MISMAS
 * instancias de Action, así que siempre disparan el mismo código.
 */
public class VentanaPrincipal extends JFrame {

    private static final String TIPO_PERSONA = "Persona Física";
    private static final String TIPO_EMPRESA = "Empresa";

    // Acciones compartidas (menú + barra de herramientas + botón)
    private final Action accionGuardar;
    private final Action accionRefrescar;
    private final Action accionUsuarios;
    private final Action accionSalir;

    // Formulario
    private final JComboBox<String> comboTipo = new JComboBox<>(new String[]{TIPO_PERSONA, TIPO_EMPRESA});
    private final JTextField campoNombre = new JTextField(20);
    private final JCheckBox checkHistorial = new JCheckBox("Tiene buen historial");
    private final JSpinner spinnerIngresos = crearSpinnerNumerico();
    private final JSpinner spinnerGastos = crearSpinnerNumerico();
    private final JLabel etiquetaRubro = new JLabel("Rubro:");
    private final JTextField campoRubro = new JTextField(20);
    private final JLabel etiquetaDirector = new JLabel("Director:");
    private final JTextField campoDirector = new JTextField(20);

    // Vista de base de datos
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Tipo", "Nombre", "Ingresos", "Gastos", "Historial", "Rubro", "Director", "Evaluación"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
 
        @Override
        public Class<?> getColumnClass(int columna) {
            switch (columna) {
                case 0: return Integer.class;
                case 3:
                case 4: return Float.class;      // ordena como número
                default: return String.class;
            }
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    // Barra de progreso
    private final JProgressBar barraProgreso = new JProgressBar();

    public VentanaPrincipal() {
        setTitle("Sistema Evaluador de Créditos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(620, 460));

        accionGuardar = new AbstractAction("Guardar") {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarCliente();
            }
        };
        accionGuardar.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        accionGuardar.putValue(Action.SHORT_DESCRIPTION, "Guardar el análisis (Ctrl+S)");

        accionRefrescar = new AbstractAction("Refrescar") {
            @Override
            public void actionPerformed(ActionEvent e) {
                refrescarListado();
            }
        };
        accionRefrescar.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        accionRefrescar.putValue(Action.SHORT_DESCRIPTION, "Recargar el listado de clientes (F5)");

        accionUsuarios = new AbstractAction("Gestión de usuarios") {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVentanaUsuarios();
            }
        };
        accionUsuarios.putValue(Action.SHORT_DESCRIPTION, "Abrir la gestión de usuarios");

        accionSalir = new AbstractAction("Salir") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        };

        setJMenuBar(crearBarraMenu());

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(crearBarraHerramientas(), BorderLayout.NORTH);
        contenido.add(crearPestanas(), BorderLayout.CENTER);
        contenido.add(crearBarraProgreso(), BorderLayout.SOUTH);
        setContentPane(contenido);

        // Comportamiento dinámico del formulario
        comboTipo.addActionListener(e -> actualizarCamposPorTipo());
        actualizarCamposPorTipo();

        pack();
        setLocationRelativeTo(null);

        // Carga inicial del listado
        SwingUtilities.invokeLater(this::refrescarListado);
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private JMenuBar crearBarraMenu() {
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);
        menuArchivo.add(new JMenuItem(accionGuardar));
        menuArchivo.add(new JMenuItem(accionRefrescar));
        menuArchivo.addSeparator();
        menuArchivo.add(new JMenuItem(accionSalir));

        JMenu menuAdmin = new JMenu("Administración");
        menuAdmin.setMnemonic(KeyEvent.VK_D);
        menuAdmin.add(new JMenuItem(accionUsuarios));

        JMenuBar barra = new JMenuBar();
        barra.add(menuArchivo);
        barra.add(menuAdmin);
        return barra;
    }

    //barra de herramientas
    private JToolBar crearBarraHerramientas() {
        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.add(accionGuardar);
        barra.add(accionRefrescar);
        barra.addSeparator();
        barra.add(accionUsuarios);
        return barra;
    }

    //JTabbedPane con las dos vistas. 
    private JTabbedPane crearPestanas() {
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Nuevo análisis", crearPanelFormulario());
        pestanas.addTab("Base de datos", crearPanelBaseDatos());
        return pestanas;
    }

    //formulario de carga
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        int fila = 0;
        agregarFila(panel, gbc, fila++, new JLabel("Tipo de cliente:"), comboTipo);
        agregarFila(panel, gbc, fila++, new JLabel("Nombre:"), campoNombre);
        agregarFila(panel, gbc, fila++, new JLabel("Historial:"), checkHistorial);
        agregarFila(panel, gbc, fila++, new JLabel("Ingresos:"), spinnerIngresos);
        agregarFila(panel, gbc, fila++, new JLabel("Gastos:"), spinnerGastos);
        agregarFila(panel, gbc, fila++, etiquetaRubro, campoRubro);
        agregarFila(panel, gbc, fila++, etiquetaDirector, campoDirector);

        // Botón de guardar: misma Action que el menú y la barra de herramientas
        gbc.gridx = 1;
        gbc.gridy = fila;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JButton(accionGuardar), gbc);

        // Empuja todo hacia arriba
        gbc.gridx = 0;
        gbc.gridy = fila + 1;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, int fila, JLabel etiqueta, java.awt.Component campo) {
        gbc.gridy = fila;
        gbc.weighty = 0;

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(etiqueta, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(campo, gbc);
    }

    /** JSpinner numérico con SpinnerNumberModel y formato de 2 decimales. */
    private static JSpinner crearSpinnerNumerico() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1_000_000_000.0, 100.0));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#,##0.00"));
        return spinner;
    }

    private JPanel crearPanelBaseDatos() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tabla.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    //barra de progreso inferior. 
    private JProgressBar crearBarraProgreso() {
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Listo");
        return barraProgreso;
    }

    // ------------------------------------------------------------------
    // Comportamiento del formulario
    // ------------------------------------------------------------------

    /** Muestra rubro y director solo cuando el tipo es Empresa. */
    private void actualizarCamposPorTipo() {
        boolean esEmpresa = TIPO_EMPRESA.equals(comboTipo.getSelectedItem());
        etiquetaRubro.setVisible(esEmpresa);
        campoRubro.setVisible(esEmpresa);
        etiquetaDirector.setVisible(esEmpresa);
        campoDirector.setVisible(esEmpresa);
        if (!esEmpresa) {
            campoRubro.setText("");
            campoDirector.setText("");
        }
        revalidate();
        repaint();
    }

    private void limpiarFormulario() {
        campoNombre.setText("");
        checkHistorial.setSelected(false);
        spinnerIngresos.setValue(0.0);
        spinnerGastos.setValue(0.0);
        campoRubro.setText("");
        campoDirector.setText("");
        comboTipo.setSelectedItem(TIPO_PERSONA);
        campoNombre.requestFocusInWindow();
    }

    // ------------------------------------------------------------------
    // Progreso + operaciones contra la base de datos
    // ------------------------------------------------------------------

    private void setOcupado(boolean ocupado) {
        barraProgreso.setIndeterminate(ocupado);
        barraProgreso.setString(ocupado ? "Procesando..." : "Listo");
        if (!ocupado) {
            barraProgreso.setValue(0);
        }
        // Evita disparar dos operaciones a la vez
        accionGuardar.setEnabled(!ocupado);
        accionRefrescar.setEnabled(!ocupado);
        accionUsuarios.setEnabled(!ocupado);
    }

    /**
     * Ejecuta una tarea (normalmente contra la BD) en un hilo aparte, con la
     * barra en modo indeterminado mientras dura. Al terminar la barra vuelve a
     * su estado normal y se ejecuta alTerminar en el hilo de Swing.
     */
    private <T> void ejecutarConProgreso(Callable<T> tarea, Consumer<T> alTerminar) {
        setOcupado(true);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tarea.call();
            }

            @Override
            protected void done() {
                try {
                    T resultado = get();
                    setOcupado(false);
                    alTerminar.accept(resultado);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    setOcupado(false);
                } catch (ExecutionException ex) {
                    setOcupado(false);
                    JOptionPane.showMessageDialog(VentanaPrincipal.this,
                            "Ocurrió un error:\n" + ex.getCause().getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void guardarCliente() {
        // Confirma lo que el usuario haya tipeado en los spinners
        try {
            spinnerIngresos.commitEdit();
            spinnerGastos.commitEdit();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Ingresos o gastos con formato inválido.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = campoNombre.getText().trim();
        boolean esEmpresa = TIPO_EMPRESA.equals(comboTipo.getSelectedItem());
        String rubro = campoRubro.getText().trim();
        String director = campoDirector.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresá el nombre.",
                    "Datos incompletos", JOptionPane.INFORMATION_MESSAGE);
            campoNombre.requestFocusInWindow();
            return;
        }
        if (esEmpresa && (rubro.isEmpty() || director.isEmpty())) {
            JOptionPane.showMessageDialog(this, "Para una empresa completá rubro y director.",
                    "Datos incompletos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        float ingresos = ((Number) spinnerIngresos.getValue()).floatValue();
        float gastos = ((Number) spinnerGastos.getValue()).floatValue();
        boolean historial = checkHistorial.isSelected();

        // instancia segun el tipo seleccionado
        final Cliente cliente = esEmpresa
                ? new Empresa(nombre, ingresos, gastos, historial, rubro, director)
                : new Personas_fis(nombre, ingresos, gastos, historial);

            ejecutarConProgreso(() -> {
            int id = GestorBD.guardarCliente(cliente);   // 1) guarda en Clientes, devuelve el id
            if (cliente instanceof Empresa) {             // 2) después, la tabla hija según el tipo
                Empresa emp = (Empresa) cliente;
                GestorBD.guardarEmpresa(id, emp.getRubro(), emp.getDirector());
            } else {
                GestorBD.guardarPersonaFisica(id);
            }
            return id;
        }, id -> {
            JOptionPane.showMessageDialog(this,
                    "Cliente guardado (ID " + id + ").\nEvaluación: "
                    + (cliente.esApto() ? "APTO para el préstamo" : "NO APTO para el préstamo"),
                    "Guardado", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            refrescarListado();
        });
    }

    private void refrescarListado() {
        ejecutarConProgreso(GestorBD::listarClientes, (List<Object[]> filas) -> {
            modeloTabla.setRowCount(0);
            for (Object[] fila : filas) {
                modeloTabla.addRow(fila);
            }
        });
    }

    //acceso a gestion de usuarios
   private void abrirVentanaUsuarios() {
        ejecutarConProgreso(GestorBD::listarUsuarios,
                usuarios -> new VentanaUsuarios(usuarios).setVisible(true));
    }
}