/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemaevaluadorcreditos;

/**
 *
 * @author ale
 */
public abstract class Cliente {
    
    
    private String nombre;
    private float ingresos;
    private float gastos;
    private boolean tieneBuenIngreso;
    
    public Cliente(String nombre, float ingresos, float gastos, boolean tieneBuenIngreso) {
        this.nombre = nombre;
        this.ingresos = ingresos;
        this.gastos = gastos;
        this.tieneBuenIngreso = tieneBuenIngreso;
    }
    
    public abstract void evaluarRiesgo();

    public String getNombre() {
        return nombre;
    }

    public float getIngresos() {
        return ingresos;
    }

    public float getGastos() {
        return gastos;
    }

    public boolean isTieneBuenIngreso() {
        return tieneBuenIngreso;
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

    public void setTieneBuenIngreso(boolean tieneBuenIngreso) {
        this.tieneBuenIngreso = tieneBuenIngreso;
    }
    
    
    
}
