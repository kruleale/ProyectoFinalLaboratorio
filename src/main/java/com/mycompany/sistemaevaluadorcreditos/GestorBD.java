package com.mycompany.sistemaevaluadorcreditos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestorBD {

    public static final String HOST = "localhost";
    public static final int PUERTO = 3306;
    public static final String BASE_DE_DATOS = "sistema_clientes";
    public static final String USUARIO = "root";
    public static final String CONTRASENA = "12341234";

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DE_DATOS;
        return DriverManager.getConnection(url, USUARIO, CONTRASENA);
    }

    // ------------------------------------------------------------------
    // Clientes
    // ------------------------------------------------------------------

    public static int guardarCliente(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO Clientes (nombre, ingresos, gastos) VALUES (?, ?, ?)";

        // try-with-resources: Connection, PreparedStatement y ResultSet se cierran
        // automáticamente al salir del bloque, haya o no excepción.
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

    // ------------------------------------------------------------------
    // Usuarios
    // ------------------------------------------------------------------

    /** Valida el login contra la tabla Usuarios. */
    public static boolean buscarCliente(String nombreUsuario, String clave) throws SQLException {
        String sql = "SELECT 1 FROM Usuarios WHERE nombre_usuario = ? AND clave = ?";

        try (Connection conexion = getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, nombreUsuario);
            sentencia.setString(2, clave);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public static void insertarCliente(String nombreUsuario, String clave) throws SQLException {
        String sql = "INSERT INTO Usuarios (nombre_usuario, clave) VALUES (?, ?)";

        try (Connection conexion = getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, nombreUsuario);
            sentencia.setString(2, clave);

            sentencia.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("El nombre de usuario ya existe.", e);
            }
            throw e;
        }
    }

    /**
     * Devuelve todos los usuarios registrados, ordenados por id.
     * La clave no se incluye en el resultado.
     */
    public static List<Usuario> listarUsuarios() throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario FROM Usuarios ORDER BY id_usuario";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conexion = getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                usuarios.add(new Usuario(
                        resultado.getInt("id_usuario"),
                        resultado.getString("nombre_usuario")));
            }
        }

        return usuarios;
    }

    /**
     * Elimina el usuario con el id indicado.
     *
     * @return true si se eliminó una fila, false si no existía ese id
     */
    public static boolean eliminarUsuario(int idUsuario) throws SQLException {
        String sql = "DELETE FROM Usuarios WHERE id_usuario = ?";

        try (Connection conexion = getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idUsuario);

            return sentencia.executeUpdate() > 0;
        }
    }
}