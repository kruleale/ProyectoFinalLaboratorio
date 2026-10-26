package com.mycompany.sistemaevaluadorcreditos;

/**
 * Representa una fila de la tabla Usuarios.
 * No incluye la clave a propósito: el listado no debería exponerla.
 */
public class Usuario {

    private final int idUsuario;
    private final String nombreUsuario;

    public Usuario(int idUsuario, String nombreUsuario) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    @Override
    public String toString() {
        return idUsuario + " - " + nombreUsuario;
    }
}