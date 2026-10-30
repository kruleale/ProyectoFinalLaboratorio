package com.mycompany.sistemaevaluadorcreditos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Ventana de gestión de usuarios.
 *
 * Estructura:
 *  - JSplitPane: a la izquierda el formulario de alta, a la derecha el listado.
 *  - Listado: JTable (con TableRowSorter) dentro de un JScrollPane, con
 *    JPopupMenu (clic derecho) y botones Eliminar / Refrescar.
 *  - JProgressBar en la parte inferior, en modo indeterminado durante
 *    cualquier operación contra la base de datos.
 *
 * Los botones y el menú contextual comparten las MISMAS instancias de Action.
 */
public class VentanaUsuarios extends JFrame {

    // Formulario de alta
    private final JTextField campoNombreUsuario = new JTextField(15);
    private final JPasswordField campoClave = new JPasswordField(15);
    private final JButton botonCrearUsuario;
    private final JButton botonEliminarUsuario;
    private final JButton botonRefrescar;

    // Listado
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Usuario"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == 0 ? Integer.class : String.class;   // ordena el ID como número
        }
    };
    private final JTable tablaUsuarios = new JTable(modeloTabla);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
    private final JPopupMenu menuContextual = new JPopupMenu();

    // Barra de progreso
    private final JProgressBar barraProgreso = new JProgressBar();

    // Acciones compartidas (botones + menú contextual)
    private final Action accionCrear;
    private final Action accionEliminar;
    private final Action accionRefrescar;

    private boolean ocupado = false;

    /** Abre la ventana y carga el listado por su cuenta. */
    public VentanaUsuarios() {
        this(null);
    }

    /**
     * Abre la ventana con un listado ya cargado (por ejemplo, el que trajo
     * VentanaPrincipal con la barra de progreso). Si es null, lo carga acá.
     */
    public VentanaUsuarios(List<Usuario> usuariosIniciales) {
        setTitle("Gestión de usuarios");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);   // cierra solo esta ventana, no la app
        setMinimumSize(new Dimension(640, 380));

        accionCrear = new AbstractAction("Crear usuario") {
            @Override
            public void actionPerformed(ActionEvent e) {
                crearUsuario();
            }
        };
        accionEliminar = new AbstractAction("Eliminar usuario") {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarUsuarioSeleccionado();
            }
        };
        accionRefrescar = new AbstractAction("Refrescar") {
            @Override
            public void actionPerformed(ActionEvent e) {
                refrescarListado();
            }
        };

        botonCrearUsuario = new JButton(accionCrear);
        botonEliminarUsuario = new JButton(accionEliminar);
        botonRefrescar = new JButton(accionRefrescar);

        configurarTabla();
        configurarMenuContextual();

        JSplitPane divisor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                crearPanelFormulario(), crearPanelListado());
        divisor.setOneTouchExpandable(true);
        divisor.setDividerLocation(250);
        divisor.setResizeWeight(0.0);       // al agrandar la ventana crece el listado

        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Listo");

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(divisor, BorderLayout.CENTER);
        contenido.add(barraProgreso, BorderLayout.SOUTH);
        setContentPane(contenido);

        actualizarEstadoAcciones();

        pack();
        setLocationRelativeTo(null);

        if (usuariosIniciales != null) {
            cargarEnTabla(usuariosIniciales);
        } else {
            refrescarListado();
        }
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nuevo usuario"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(campoNombreUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Clave:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(campoClave, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(botonCrearUsuario, gbc);

        // Empuja todo hacia arriba
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        // Enter en la clave = Crear usuario
        campoClave.addActionListener(e -> {
            if (accionCrear.isEnabled()) {       // no permitir alta mientras hay una operación en curso
                accionCrear.actionPerformed(e);
            }
        });

        return panel;
    }

    private JPanel crearPanelListado() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(botonEliminarUsuario);
        botones.add(botonRefrescar);
        panel.add(botones, BorderLayout.SOUTH);

        return panel;
    }

    private void configurarTabla() {
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setRowSorter(sorter);
        sorter.setSortKeys(Collections.singletonList(
                new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(80);

        // El botón Eliminar solo se habilita si hay una fila seleccionada
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarEstadoAcciones();
            }
        });
    }

    /** JPopupMenu: clic derecho sobre la tabla. Usa las mismas Actions que los botones. */
    private void configurarMenuContextual() {
        menuContextual.add(new JMenuItem(accionEliminar));
        menuContextual.add(new JMenuItem(accionRefrescar));

        tablaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mostrarMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mostrarMenu(e);
            }

            private void mostrarMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                // Selecciona la fila sobre la que se hizo clic derecho
                int fila = tablaUsuarios.rowAtPoint(e.getPoint());
                if (fila >= 0) {
                    tablaUsuarios.setRowSelectionInterval(fila, fila);
                } else {
                    tablaUsuarios.clearSelection();
                }
                menuContextual.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    // ------------------------------------------------------------------
    // Progreso + operaciones contra la base de datos
    // ------------------------------------------------------------------

    private void setOcupado(boolean valor) {
        ocupado = valor;
        barraProgreso.setIndeterminate(valor);
        barraProgreso.setString(valor ? "Procesando..." : "Listo");
        if (!valor) {
            barraProgreso.setValue(0);
        }
        actualizarEstadoAcciones();
    }

    private void actualizarEstadoAcciones() {
        accionCrear.setEnabled(!ocupado);
        accionRefrescar.setEnabled(!ocupado);
        accionEliminar.setEnabled(!ocupado);
    }

    /**
     * Ejecuta una tarea en un hilo aparte, con la
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
                    JOptionPane.showMessageDialog(VentanaUsuarios.this,
                            "Ocurrió un error:\n" + ex.getCause().getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    private void crearUsuario() {
        String nombre = campoNombreUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());

        if (nombre.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completá usuario y clave.",
                    "Datos incompletos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ejecutarConProgreso(() -> {
            GestorBD.insertarCliente(nombre, clave);
            return null;
        }, resultado -> {
            JOptionPane.showMessageDialog(this, "Usuario \"" + nombre + "\" creado.",
                    "Usuario creado", JOptionPane.INFORMATION_MESSAGE);
            campoNombreUsuario.setText("");
            campoClave.setText("");
            campoNombreUsuario.requestFocusInWindow();
            refrescarListado();
        });
    }

    private void eliminarUsuarioSeleccionado() {
        int filaVista = tablaUsuarios.getSelectedRow();
        if (filaVista < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná un usuario de la tabla.",
                    "Sin selección", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Con TableRowSorter, la fila de la vista puede no coincidir con la del modelo
        int filaModelo = tablaUsuarios.convertRowIndexToModel(filaVista);
        int idUsuario = (Integer) modeloTabla.getValueAt(filaModelo, 0);
        String nombre = (String) modeloTabla.getValueAt(filaModelo, 1);

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al usuario \"" + nombre + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutarConProgreso(() -> GestorBD.eliminarUsuario(idUsuario), eliminado -> {
            if (!eliminado) {
                JOptionPane.showMessageDialog(this, "El usuario ya no existía.",
                        "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
            refrescarListado();
        });
    }

    private void refrescarListado() {
        ejecutarConProgreso(GestorBD::listarUsuarios, this::cargarEnTabla);
    }

    private void cargarEnTabla(List<Usuario> usuarios) {
        modeloTabla.setRowCount(0);
        for (Usuario u : usuarios) {
            modeloTabla.addRow(new Object[]{u.getIdUsuario(), u.getNombreUsuario()});
        }
        actualizarEstadoAcciones();
    }
} 

