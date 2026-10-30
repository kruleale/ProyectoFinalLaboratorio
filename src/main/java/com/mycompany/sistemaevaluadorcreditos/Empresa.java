package com.mycompany.sistemaevaluadorcreditos;

public class Empresa extends Cliente {
    
    
    public static final double UMBRAL_RATIO = 1.5;

    private String rubro;
    private String director;

    public Empresa(String nombre, float ingresos, float gastos, boolean tieneBuenIngreso,
                   String rubro, String director) {
        super(nombre, ingresos, gastos, tieneBuenIngreso);
        this.rubro = rubro;
        this.director = director;
    }

    @Override
    public double evaluarRiesgo() {
        double ratio;
        if (getGastos() == 0) {                           // evita dividir por cero
            ratio = getIngresos() > 0 ? Double.POSITIVE_INFINITY : 0.0;
        } else {
            ratio = getIngresos() / getGastos();
        }
        return ratio >= UMBRAL_RATIO ? 1.0 : 0.0;
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