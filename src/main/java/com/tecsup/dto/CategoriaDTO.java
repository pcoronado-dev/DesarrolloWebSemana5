package com.tecsup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;

    public CategoriaDTO() {
    }

    public CategoriaDTO(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
