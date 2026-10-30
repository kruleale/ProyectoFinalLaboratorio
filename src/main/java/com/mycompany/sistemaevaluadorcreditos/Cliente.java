/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemaevaluadorcreditos;

/**
 * santi papu
 * @author ale
 */
public abstract class Cliente {
    
    
    private String nombre;
    private float ingresos;
    private float gastos;
    private boolean tieneBuenHistorial;
    
    public Cliente(String nombre, float ingresos, float gastos, boolean tieneBuenHistorial) {
        this.nombre = nombre;
        this.ingresos = ingresos;
        this.gastos = gastos;
        this.tieneBuenHistorial = tieneBuenHistorial;
    }
    
    public abstract double evaluarRiesgo();
    
    public boolean esApto() {
        return evaluarRiesgo() > 0;
    }

    public String getNombre() {
        return nombre;
    }

    public float getIngresos() {
        return ingresos;
    }

    public float getGastos() {
        return gastos;
    }

    public boolean isTieneBuenHistorial() {
        return tieneBuenHistorial;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setIngresos(float ingresos) {
        this.ingresos = ingresos;
    }

    public void setGastos(float gastos) {
        this.gastos = gastos;
    }

    public void setTieneBuenHistorial(boolean tieneBuenHistorial) {
        this.tieneBuenHistorial = tieneBuenHistorial;
    }
    
    
    
}
