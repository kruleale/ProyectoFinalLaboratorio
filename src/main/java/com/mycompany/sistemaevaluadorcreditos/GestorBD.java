
package com.mycompany.sistemaevaluadorcreditos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GestorBD {

    public static final String HOST = "localhost";
    public static final int PUERTO = 3306;
    public static final String USUARIO = "root";
    public static final String CONTRASENA = "12341234";

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + HOST + ":" + PUERTO + "/sistemaevaluadorcreditos";

        return DriverManager.getConnection(url, USUARIO, CONTRASENA);
    }

    public static int guardarCliente(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, ingresos, gastos) VALUES (?, ?, ?)";

        try (Connection conexion = getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(
                 sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            sentencia.setString(1, cliente.getNombre());
            sentencia.setFloat(2, cliente.getIngresos());
            sentencia.setFloat(3, cliente.getGastos());

            sentencia.executeUpdate();

            try (ResultSet resultado = sentencia.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID del cliente.");
    }

    public static boolean buscarCliente(String nombre, String contrasena) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE nombre = ? AND contrasena = ?";

        try (Connection conexion = getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, nombre);
            sentencia.setString(2, contrasena);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }
    
    public static void insertarCliente(){}
}
