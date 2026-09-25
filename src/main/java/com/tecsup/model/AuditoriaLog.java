package com.tecsup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String accion;

    @Column(length = 100)
    private String metodo;

    private LocalDateTime fecha;

    @Column(length = 255)
    private String detalle;

    @Column(length = 100)
    private String usuario;

    public AuditoriaLog() {
    }

    public AuditoriaLog(String accion, String metodo, String detalle, String usuario) {
        this.accion = accion;
        this.metodo = metodo;
        this.fecha = LocalDateTime.now();
        this.detalle = detalle;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}