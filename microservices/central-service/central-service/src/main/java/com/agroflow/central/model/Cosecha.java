package com.agroflow.central.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class Cosecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long agricultorId;
    private String tipoProducto;
    private double cantidadKg;
    private String estado;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAgricultorId() {
        return agricultorId;
    }

    public void setAgricultorId(Long agricultorId) {
        this.agricultorId = agricultorId;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public double getCantidadKg() {
        return cantidadKg;
    }

    public void setCantidadKg(double cantidadKg) {
        this.cantidadKg = cantidadKg;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}