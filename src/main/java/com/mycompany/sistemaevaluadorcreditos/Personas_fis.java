/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemaevaluadorcreditos;

/**
 *
 * @author usrlocal
 */ 
    
public class Personas_fis extends Cliente {
    
    public static final double UMBRAL_MARGEN = 0.30;

    public Personas_fis(String nombre, float ingresos, float gastos, boolean tieneBuenIngreso) {
        super(nombre, ingresos, gastos, tieneBuenIngreso);
    }

    @Override
    public double evaluarRiesgo() {
        if (!isTieneBuenHistorial()) {
            return 0.0;                                   // sin buen historial: no apto
        }
        double margen = getIngresos() - getGastos();
        return margen >= getIngresos() * UMBRAL_MARGEN ? 1.0 : 0.0;
    }
}
    
    

