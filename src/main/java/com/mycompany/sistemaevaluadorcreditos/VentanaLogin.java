/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemaevaluadorcreditos;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
 
public class VentanaLogin extends JFrame {
 
    private final JTextField campoUsuario;
    private final JPasswordField campoClave;
    private final JButton botonIngresar;
 
    public VentanaLogin() {
        setTitle("Sistema Evaluador de Créditos - Ingreso");
        setSize(380, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);     // al cerrar, termina la app
        setResizable(false);
        setLocationRelativeTo(null);
 
        // Grilla de 3 filas x 2 columnas: etiquetas a la izquierda, campos a llenar a la derecha
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
 
        campoUsuario = new JTextField();
        campoClave = new JPasswordField();
        botonIngresar = new JButton("Ingresar");
 
        panel.add(new JLabel("Usuario:"));
        panel.add(campoUsuario);
        panel.add(new JLabel("Clave:"));
        panel.add(campoClave);
        panel.add(new JLabel());                     // celda vacia para alinear el boton
        panel.add(botonIngresar);
 
        add(panel);
 
        botonIngresar.addActionListener(e -> validarAcceso());
        getRootPane().setDefaultButton(botonIngresar);
    }
 
    private void validarAcceso() {
        String usuario = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());
 
        if (usuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Complete usuario y clave.",
                    "Datos incompletos",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
 
        try {
            boolean accesoConcedido = GestorBD.buscarCliente(usuario, clave);
 
            if (accesoConcedido) {
                dispose();                           
                new VentanaPrincipal().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,"Usuario o clave incorrectos.", "Acceso denegado",JOptionPane.WARNING_MESSAGE);
                campoClave.setText("");
                campoClave.requestFocus();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,"No se pudo conectar con la base de datos:\n" + ex.getMessage(),
                    "Error de conexión",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
 
    
}