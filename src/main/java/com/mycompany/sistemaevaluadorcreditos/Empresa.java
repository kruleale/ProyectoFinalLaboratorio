package com.mycompany.sistemaevaluadorcreditos;

public class Empresa extends Cliente {

    private String rubro;
    private String director;

    public Empresa(String nombre, float ingresos, float gastos, boolean tieneBuenIngreso,
                   String rubro, String director) {
        super(nombre, ingresos, gastos, tieneBuenIngreso);
        this.rubro = rubro;
        this.director = director;
    }

    @Override
    public void evaluarRiesgo() {
        // Aquí se implementará la evaluación del riesgo.
    }

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }
}